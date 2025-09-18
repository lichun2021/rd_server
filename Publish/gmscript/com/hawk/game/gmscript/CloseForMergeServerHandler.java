package com.hawk.game.gmscript;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.hawk.log.HawkLog;
import org.hawk.net.HawkNetworkManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.tuple.HawkTuple2;

import com.google.common.collect.Table;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.config.NationTechCfg;
import com.hawk.game.config.SuperWeaponConstCfg;
import com.hawk.game.entity.GuildBuildingEntity;
import com.hawk.game.entity.PlayerGuardInviteEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.manor.building.AbstractGuildBuilding;
import com.hawk.game.guild.manor.building.GuildManorWarehouse;
import com.hawk.game.guild.manor.building.IGuildBuilding;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.msg.SessionClosedMsg;
import com.hawk.game.nation.NationService;
import com.hawk.game.nation.mission.NationMissionCenter;
import com.hawk.game.nation.ship.NationShipFactory;
import com.hawk.game.nation.tech.NationTechCenter;
import com.hawk.game.nation.tech.NationTechResearchTemp;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.Mail.MailLiteInfo;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.game.protocol.RedisMail.MailEntityContent;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponPeriod;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.MailService;
import com.hawk.game.service.RelationService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.starwars.StarWarsOfficerService;
import com.hawk.game.superweapon.SuperWeaponService;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.WorldTaskType;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;

public class CloseForMergeServerHandler extends HawkScript {

	@Override
	public String action(Map<String, String> arg0, HawkScriptHttpInfo arg1) {
		try {
			//关闭网络,踢玩家下线.
			closeNetwork();
			//先加载一次玩家.
			reloadStarWarsOfficer();
			//遣返行军.
			returnMarch();
			
			//清理跨服的buf
			clearCrossBuff();
			
			//联盟仓库
			backGuildStorage();
			
			//活动发奖处理.
			sendActivityRewardForMergeServer();
			
			//发送战区的奖励.
			sendSuperWeaponReward();
			
			//处理未处理的守护申请.
			handleGuardInvite();
			
			//处理合服的的星球大战官职
			handleStarWars();
			
			//处理写入的国王信息
			handlePresident();
			
			// 处理年兽k值
			handleNianK();
			
			// 处理飞船制造厂
			handleNationShipFactory();
			
			// 国家任务
			nationMisson();
			
			// 国家科技
			nationTech();
			
			HawkLog.logPrintln("close for merge server success");
			
			return HawkScript.successResponse("ok");
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		HawkLog.logPrintln("close for merge server fail");
		
		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "occur exception");
	}
	
	
	
	/**
	 * 清理国王
	 */
	private void handlePresident() {
		//清理掉本服的国王.
		RedisProxy.getInstance().deleteServerKing(GsConfig.getInstance().getServerId());
		RedisProxy.getInstance().deleteCrossServerKing(GsConfig.getInstance().getServerId());
	}



	private void reloadStarWarsOfficer() {
		try {
			//先加载一下官职.
			StarWarsOfficerService.getInstance().loadOrReloadOfficer();
		} catch (Exception e) {
			HawkException.catchException(e);
		}		
	}
	
	public int getType(int part, int team) {
		if (part == 0) {
			return GsConst.StarWarsConst.UNSET_OFFICER_WORLD_KING;
		} else {
			if (team == 0) {
				return GsConst.StarWarsConst.UNSET_OFFICER_PART_KING;
			} else {
				return GsConst.StarWarsConst.UNSET_OFFICER_COMMON;
			}
		}
	}
	private void handleStarWars() {
		HawkLog.logPrintln("handle star wars officer");
		try {
			//先加载一遍		
			Map<String, List<HawkTuple2<Integer, Integer>>> localMap = StarWarsOfficerService.getInstance().getPlayerPartOfficerMap();					
			GlobalData globalData = GlobalData.getInstance();
			Map<String, Integer> playerIdBitMap = new HashMap<>();
			for (Entry<String, List<HawkTuple2<Integer, Integer>>> entry : localMap.entrySet()) {				//
				if (!globalData.isExistPlayerId(entry.getKey())) {
					continue;
				}
				int bitValue  = 0;
				for (HawkTuple2<Integer, Integer> tuple : entry.getValue()) {
					bitValue |= this.getType(tuple.first, tuple.second);
					HawkLog.logPrintln("merge server delete officer id:{} part:{} team:{}", entry.getKey(), tuple.first, tuple.second);
					RedisProxy.getInstance().deleteStarWarsOfficer(tuple.first, tuple.second);
				}
				playerIdBitMap.put(entry.getKey(), bitValue);
			}
					 																		
			if (!playerIdBitMap.isEmpty()) {
				HawkLog.logPrintln("merge server unset officer playeridset:{}", playerIdBitMap);							
				RedisProxy.getInstance().updateMergeServerUnsetOfficerInfo(playerIdBitMap);
			}											
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}				
	}


	private void handleGuardInvite() {
		HawkLog.logPrintln("handle guard invite");
		Map<String, PlayerGuardInviteEntity> guardInviteMap = RelationService.getInstance().getGuardInviteMap();
		Map<String, PlayerGuardInviteEntity> inviteMap = new HashMap<>(guardInviteMap);
		//先把原来申请map 清理.
		guardInviteMap.clear();
		LocalRedis.getInstance().deleteAllGuardInvite();
		
		for (PlayerGuardInviteEntity entity : inviteMap.values()) {
			try {
				RelationService.getInstance().returnPlayerInviteCost(entity);
			} catch (Exception e) {
				HawkException.catchException(e);
			}			
		}
		
		
	}


	private void sendSuperWeaponReward() {
		HawkLog.logPrintln("execute send super waepon season reward method");
		SuperWeaponService  superWeaponService = SuperWeaponService.getInstance();
		int seasonTurn = superWeaponService.getSeasonTurn();
		int turnMax = SuperWeaponConstCfg.getInstance().getSeasonPeriodNum();
		int status = superWeaponService.getStatus();
		if (seasonTurn > 0 && !(seasonTurn % turnMax == 0 && status == SuperWeaponPeriod.CONTROL_VALUE)) {			 
			HawkLog.logPrintln("send super waepon season reward real");
			superWeaponService.sendSWSectionAward();			
		} 
		
	}


	private void sendActivityRewardForMergeServer() {
		try {
			Map<Integer, ActivityBase> map = ActivityManager.getInstance().getActivityMap();
			HawkLog.logPrintln("send activity reward by merge server activityIds:{}", map.keySet());
			Map<String, String> rewardedMap = LocalRedis.getInstance().getActivitySendRewardRecord();
			HawkLog.logPrintln("send activity reward by merge server rewardedMap:{}", rewardedMap);
			Map<String, String> warpMap = new ConcurrentHashMap<String, String>(rewardedMap);
			Map<Integer, Boolean> finishMap = new ConcurrentHashMap<Integer, Boolean>();
			for (Entry<Integer, ActivityBase> entry : map.entrySet()) {
				//已经调用过了，不再调用.
				if (!HawkOSOperator.isEmptyString(warpMap.get(entry.getKey()+""))) {
					continue;
				}
					
				finishMap.put(entry.getKey(), false);
				HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
					
					@Override
					public Object run() {
						try {
							boolean result = entry.getValue().handleForMergeServer();
							if (result) {
								warpMap.put(entry.getKey()+"", "reward");
								HawkLog.logPrintln("send activity reward by merge server success activityId:{}", entry.getKey());
							}
							
							return null;
						} catch (Exception e) {
							HawkException.catchException(e);
						} finally {
							finishMap.put(entry.getKey(), true);
							boolean allFinish = true;
							for (Boolean result : finishMap.values()) {
								if (!result) {
									allFinish = false;
									break;
								}
							}
							
							if (allFinish) {
								HawkLog.logPrintln("all acitivyt send reward by merge server finish ");
								LocalRedis.getInstance().putActivitySendRewardRecord(warpMap);
							}
						}
						return null;
					}									
				});				
			}					
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}


	private void backGuildStorage() {
		try {
			
			GuildService guildService = GuildService.getInstance();
			List<String> guildIds = new ArrayList<>(guildService.getGuildIds());
			Field resField = GuildManorWarehouse.class.getDeclaredField("playerDeposit");
			resField.setAccessible(true);
			String title = "系统消息";
			String content = "联盟仓库资源返还";
			AtomicInteger guildCounter = new AtomicInteger();
			for (String guildId : guildIds) {
				HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
					
					@Override
					public Object run() {
						try {
							GuildManorWarehouse wareHouse = GuildManorService.getInstance().getBuildingByTypeAndIdx(guildId, 1, TerritoryType.GUILD_STOREHOUSE);
							if (wareHouse != null) {										
								try {
									@SuppressWarnings("unchecked")
									Table<String, String, Integer> playerDeposit = (Table<String, String, Integer>) resField.get(wareHouse);
									for (String playerId : playerDeposit.rowKeySet()) {
										try {
											String res = wareHouse.playerDeposit(playerId);
											List<ItemInfo> itemList = ItemInfo.valueListOf(res);
											boolean hasReward = false;
											for (ItemInfo itemInfo : itemList) {
												if (itemInfo.getCount() > 0) {
													hasReward = true;
												}
											}
											if (hasReward) {
												MailParames mailParames = MailParames.newBuilder()
										                .setMailId(MailId.REWARD_MAIL)
										                .setPlayerId(playerId)
										                .setAwardStatus(MailRewardStatus.NOT_GET)
										                .addSubTitles(title)
										                .addContents(content)
										                .addRewards(itemList)
										                .build();
												sendMail(mailParames);
											}											
										} catch (Exception e) {
											HawkException.catchException(e);
										}										
									}
									
									cleanGuildManor(wareHouse);
								} catch (Exception e) {
									HawkException.catchException(e);
								}																
							}
						} catch (Exception e) {
							HawkException.catchException(e);
						} finally {
							guildCounter.incrementAndGet();
							HawkLog.logPrintln("back stroage finish guildId:{}", guildId);
						}
						
						return null;
					}									
				});							
			}
			
			long startTime = HawkTime.getMillisecond(); 
			while(guildCounter.get() < guildIds.size() && HawkTime.getMillisecond() - startTime < 5 * 60 * 1000) {
				HawkOSOperator.sleep();
			}			
			HawkLog.logPrintln("back storage finish guildIdSize:{}", guildIds.size());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
	}
	
	private void sendMail(MailParames parames) {
		String playerId = parames.getPlayerId();
		final String mailUUID = parames.getUuid();
		final long createTime = HawkTime.getMillisecond();

		MailLiteInfo.Builder liteBuilder = MailLiteInfo.newBuilder();
		liteBuilder.setId(mailUUID);
		liteBuilder.setType(MailService.getInstance().getMailType(parames.getMailId()));
		liteBuilder.setMailId(parames.getMailId().getNumber());
		liteBuilder.setCtime(createTime);
		liteBuilder.addIcon(parames.getIcon());
		liteBuilder.setPlayerId(playerId);
		liteBuilder.setGlobalMailId(parames.getGlobalMailId());
		liteBuilder.setTitle(parames.getTitle());
		liteBuilder.setSubTitle(parames.getSubTitle());
		liteBuilder.addAllTips(parames.getTips());
		liteBuilder.setAdditionalParam(parames.getAdditionalParam());
		liteBuilder.setHasReward(false);
		liteBuilder.setLock(0);
		liteBuilder.setStatus(0);
		liteBuilder.setSendServerId(GsConfig.getInstance().getServerId());

		String reward = parames.getReward();
		if (StringUtils.isNotEmpty(reward)) {
			liteBuilder.setReward(reward);
			if (parames.getAwardStatus() != MailRewardStatus.GET) {
				liteBuilder.setHasReward(true);
			}
		}

		MailLiteInfo mailLiteInfo = liteBuilder.build();
		MailEntityContent mailEntityContent = parames.getContent();
		boolean result = false;
		try {
			Method method = MailService.class.getDeclaredMethod("addMailContent", MailLiteInfo.class, MailEntityContent.class);
			method.setAccessible(true);
			method.invoke(MailService.getInstance(), mailLiteInfo, mailEntityContent);
			result = true;
		} catch (Exception e) {
			HawkException.catchException(e);			
		}		
		HawkLog.logPrintln("back storage playerId:{}, reward:{}, result:{}", playerId, parames.getReward(), result);
	}
	private void cleanGuildManor(GuildManorWarehouse wareHouse) {
		try {
			Field field = AbstractGuildBuilding.class.getDeclaredField("entity");
			field.setAccessible(true);
			GuildBuildingEntity entity = (GuildBuildingEntity) field.get(wareHouse);						
			entity.setBuildParam("");			
			
			Field guildBuildingField = entity.getClass().getDeclaredField("buildingObj");
			guildBuildingField.setAccessible(true);
			IGuildBuilding guildBuilding  = (IGuildBuilding)guildBuildingField.get(entity);
			guildBuilding.parseBuildingParam("");						
		} catch (Exception e) {
			HawkException.catchException(e);
		} 
	}
	private void clearCrossBuff() {
		HawkLog.logPrintln("close server for mege server  clean cross buff");
		try {
			RedisProxy.getInstance().removeCRankBuff();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	public void closeNetwork() {
		HawkLog.logPrintln("close server for mege server  close net work");
		try {
			Set<Player> activePlayers = GlobalData.getInstance().getOnlinePlayers();
			for (Player player : activePlayers) {				
				try {
					player.notifyPlayerKickout(Status.SysError.SERVER_CLOSE_VALUE, "");
					GsApp.getInstance().postMsg(player, SessionClosedMsg.valueOf());
				} catch (Exception e) {
					HawkException.catchException(e);
				}				
			}
			
			Thread.sleep(3000l);
			HawkNetworkManager.getInstance().close();
		} catch (Exception e) {
			HawkException.catchException(e);
		}		
		
	}
	
	public void returnMarch() {
		HawkLog.logPrintln("close server for mege server  return all march");
		try {
			WorldMarchService marchService = WorldMarchService.getInstance();
			List<IWorldMarch> marchs = new ArrayList<>(marchService.getMarchsValue());
			Set<String> playerIdSet = new HashSet<>();
			for (IWorldMarch march : marchs) {
				playerIdSet.add(march.getPlayerId());			
			}
			
			AtomicInteger ai = new AtomicInteger();
			for (String playerId : playerIdSet) {
				try {
					WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(WorldTaskType.CLOSE_FOR_MERGE_SERVER) {
						
						@Override
						public boolean onInvoke() {
							try {
								WorldPlayerService.getInstance().moveCity(playerId, false, true);
							} catch (Exception e) {
								HawkException.catchException(e);
							} finally {
								ai.incrementAndGet();
							}							
							return true;
						}
					});
				} catch (Exception e) {
					HawkException.catchException(e);
				}	
			}
			
			//五分钟或者任务跑完
			long startTime = HawkTime.getMillisecond(); 
			while(ai.get() < playerIdSet.size() && HawkTime.getMillisecond() - startTime < 5 * 60 * 1000) {
				HawkOSOperator.sleep();
			}
			HawkLog.logPrintln("return march finish size:{}", playerIdSet.size());
		} catch (Exception e) {
			HawkException.catchException(e);
		}	
	}
	
	/**
	 * 合服前把k值存下来
	 */
	private void handleNianK() {
		int k = RedisProxy.getInstance().getNianLastK(GsConfig.getInstance().getServerId());
		if (k > 0) {
			// 这里改成直接调用redis，是因为目前有代码没更新出去，就先更新脚本的情况
			String key = "merge_nian_k" + ":" + GsConfig.getInstance().getServerId();
			RedisProxy.getInstance().getRedisSession().setString(key, String.valueOf(k));
			RedisProxy.getInstance().setNianLastK(GsConfig.getInstance().getServerId(), 0);
		}
	}
	
	/**
	 * 处理正在升级中的飞船部件，取消并返还资源
	 */
	private void handleNationShipFactory() {
		try {
			NationShipFactory shipFactory = (NationShipFactory) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_SHIP_FACTORY);
			// 判断是否已经有正在升级中得部件
			if(shipFactory == null || shipFactory.getCurrentUpEntity() == null) {
				return;
			}
			// 投递到世界线程取消
			WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.NATIONAL_SHIP_CANCEL_UPGRADE) {
				@Override
				public boolean onInvoke() {
					// 开始取消
					shipFactory.cancelUpShipPart(null);
					return true;
				}
			});
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 国家任务
	 */
	private void nationMisson() {
		try {
			NationMissionCenter missionCenter = (NationMissionCenter) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_QUEST_CENTER);
			if (missionCenter != null) {
				missionCenter.clearForMerge();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 国家科技
	 */
	private void nationTech() {
		try {
			// 建筑判断
			NationTechCenter center = (NationTechCenter)NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_TECH_CENTER);
			if (center == null || center.getLevel() <= 0) {
				return;
			}
			
			// 清除技能
			center.clearNationTechSkill();
			
			// 取消研究
			NationTechResearchTemp currTechResearch = center.getNationTechResearch();
			if (currTechResearch != null) {
				center.updateNationTechResearchInfo(null);
				center.exitRunningState();
				center.boardcastBuildState();
				NationTechCfg nationTechCfg = AssembleDataManager.getInstance().getNationTech(currTechResearch.getTechCfgId(), currTechResearch.getTarLevel());
				center.changeNationTechValue(nationTechCfg.getTechCost());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}
	
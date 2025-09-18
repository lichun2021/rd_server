package com.hawk.game.service.simulatewar;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.hawk.annotation.MessageHandler;
import org.hawk.app.HawkAppCfg;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.uuid.HawkUUIDGenerator;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.hawk.game.GsConfig;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.TemporaryMarch;
import com.hawk.game.battle.battleIncome.impl.PvpBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.SimulateWarConstCfg;
import com.hawk.game.config.SimulateWarEncourageCfg;
import com.hawk.game.config.SimulateWarRewarCfg;
import com.hawk.game.config.SimulateWarTimeCfg;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Const.BattleSkillType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Hero.PBHeroInfo;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.SimulateWar.PBSimulateWarBattleData;
import com.hawk.game.protocol.SimulateWar.PBSimulateWarPlayer;
import com.hawk.game.protocol.SimulateWar.PBSimulateWarSoldier;
import com.hawk.game.protocol.SimulateWar.PBSimulateWarWayPlayer;
import com.hawk.game.protocol.SimulateWar.SimulateWarActivityState;
import com.hawk.game.protocol.SimulateWar.SimulateWarActivityStateInfo;
import com.hawk.game.protocol.SimulateWar.SimulateWarAllMarchResp;
import com.hawk.game.protocol.SimulateWar.SimulateWarBasePlayerStruct;
import com.hawk.game.protocol.SimulateWar.SimulateWarBattelInfo;
import com.hawk.game.protocol.SimulateWar.SimulateWarBattleList;
import com.hawk.game.protocol.SimulateWar.SimulateWarBattlePlayer;
import com.hawk.game.protocol.SimulateWar.SimulateWarBattleRecordResp;
import com.hawk.game.protocol.SimulateWar.SimulateWarEncoureageInfoResp;
import com.hawk.game.protocol.SimulateWar.SimulateWarGuildBattle;
import com.hawk.game.protocol.SimulateWar.SimulateWarGuildInfo;
import com.hawk.game.protocol.SimulateWar.SimulateWarPageInfoResp;
import com.hawk.game.protocol.SimulateWar.SimulateWarPageInfoUpdate;
import com.hawk.game.protocol.SimulateWar.SimulateWarWayPlayersResp;
import com.hawk.game.protocol.SimulateWar.WayNumInfo;
import com.hawk.game.protocol.SimulateWar.WayType;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.simulatewar.data.SimulateWarActivityData;
import com.hawk.game.service.simulatewar.data.SimulateWarGuildData;
import com.hawk.game.service.simulatewar.data.SimulateWarMatchInfo;
import com.hawk.game.service.simulatewar.data.SimulateWarPlayer;
import com.hawk.game.service.simulatewar.data.SimulateWarPlayerExt;
import com.hawk.game.service.simulatewar.msg.SimulateWarAdjustWayMsg;
import com.hawk.game.service.simulatewar.msg.SimulateWarDissolveMsg;
import com.hawk.game.service.simulatewar.msg.SimulateWarEncourageMsg;
import com.hawk.game.service.simulatewar.msg.SimulateWarOrderAdjustMsg;
import com.hawk.game.service.simulatewar.msg.SimulateWarQuitGuildMsg;
import com.hawk.game.service.simulatewar.msg.SimulateWarSaveMarchMsg;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.MapUtil;
import com.hawk.game.world.march.IWorldMarch;

/**
 * 攻防模拟战.
 * @author jm
 *
 */
public class SimulateWarService extends HawkAppObj {
	
	private static final Logger logger = LoggerFactory.getLogger("Server");
	/**
	 * 活动数据
	 */
	private SimulateWarActivityData activityInfo = new SimulateWarActivityData();
	
	private static SimulateWarService instance = null;
	/**
	 * {guildId, GuildData}
	 */
	private Map<String, SimulateWarGuildData> guildData = new ConcurrentHashMap<>();
	/**
	 * 和本服的的联盟匹配的联盟.
	 */
	private Map<String, SimulateWarGuildData> enemyGuildData = new ConcurrentHashMap<>();
	/**
	 * 记录每个区服对应匹配到的区服。
	 */
	private Map<String, SimulateWarMatchInfo> guildMatchMap = new ConcurrentHashMap<>();  
	/**
	 * {playerId, PBSimulateWarPlayer.Builder}
	 */
	private Map<String, PBSimulateWarPlayer.Builder> playerDataMap = new ConcurrentHashMap<>();
	/**
	 * 存储每个工会的线路信息.
	 * {guildId, Map{wayType, idList}}
	 */
	private Map<String, Map<WayType, List<String>>> guildWayMap = new ConcurrentHashMap<>();
	/**
	 * 战斗
	 */
	private Map<String, Map<WayType, SimulateWarBattleList>> guildBattleRecordMap = new ConcurrentHashMap<>(); 
	
	public static SimulateWarService getInstance(){
		return instance; 
	}
	
	public SimulateWarService(HawkXID xid) {
		super(xid);
		instance = this;
	}
	
	/**
	 * 初始化
	 * @return
	 */
	public boolean init() {
		int perio = SimulateWarConstCfg.getInstance().getPeriodTime();
		try {
			activityInfo = RedisProxy.getInstance().getSimulateWarActivityData();
			//加载数据还是以老的状态为准.
			initLoadData();
			//检测一下状态切换.
			checkStateChange();
			
			this.addTickable(new HawkPeriodTickable(perio, perio) {

				@Override
				public void onPeriodTick() {			
					stateTick();
					
					
					if (activityInfo.getState() == SimulateWarActivityState.SW_MANAGE) {
						try {
							manageTick();
						} catch (Exception e) {
							HawkException.catchException(e);
						}
					} else if (activityInfo.getState() == SimulateWarActivityState.SW_MARCH) {
						try {
							marchTick();
						} catch (Exception e) {
							HawkException.catchException(e);
						}						
					}																				
				}

				
				
			});
		} catch (Exception e) {
			HawkException.catchException(e);
			
			return false;
		}		
		
		return true; 
	}
		
	/**
	 * 起服的时候加载数据.
	 */
	private void initLoadData() {
		int termId = this.activityInfo.getTermId();			
		this.loadLocalSignGuildData(true);
		this.loadAllEnemyGuildData(true);
		this.loadMatchInfo(true);
		Map<String, SimulateWarGuildData> warGuildDataMap = this.guildData;
		for (SimulateWarGuildData swgd :  warGuildDataMap.values()) {
			//加载敌对联盟的信息
			Map<WayType, List<String>> memMap = this.getGuildWayMarch(swgd.getGuildId());
			if (MapUtils.isEmpty(memMap)) {				
				Map<WayType, List<String>> wayMarchListMap = RedisProxy.getInstance().getSimulateWarAllWayMarch(termId, swgd.getGuildId());
				this.setGuildWayMarch(swgd.getGuildId(), wayMarchListMap);
				
				//把对战的玩家信息加载到玩家里面去.
				Map<String, PBSimulateWarPlayer.Builder> playerMap = RedisProxy.getInstance().getSimulateWarGuildAllPlayer(termId, swgd.getGuildId());
				this.playerDataMap.putAll(playerMap);
			}	
		}
		
	}
		

	/**
	 * 阶段轮询
	 */
	public void stateTick() {
		try {
			checkStateChange();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 状态切换.
	 */
	private void checkStateChange() {
		SimulateWarActivityData newInfo = calcInfo();
		int old_term = activityInfo.getTermId();
		int new_term = newInfo.getTermId();
		//同期数才能继承这个flag
		if (old_term == new_term) {
			newInfo.setStageFlag(activityInfo.getStageFlag());
		} 		

		// 如果当前期数和当前实际期数不一致,且当前活动强制关闭,则推送活动状态,且刷新状态信息
		if (old_term != new_term && new_term == 0) {
			activityInfo = newInfo;
			activityInfo.saveToRedis();
			
			// 在线玩家推送活动状态
			for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
				try {
					syncPageInfo(player);
				} catch (Exception e) {
					HawkException.catchException(e);
				}				
			}					
		}
		SimulateWarActivityState old_state = activityInfo.getState();
		SimulateWarActivityState new_state = newInfo.getState();
		boolean needUpdate = false;
		// 期数不一致,则重置活动状态,从未开启阶段开始轮询
		if (new_term != old_term) {
			old_state = SimulateWarActivityState.SW_NOT_OPEN;
			activityInfo.setTermId(new_term);
			needUpdate = true;
		}

		for (int i = 0; i < 8; i++) {
			if (old_state == new_state) {
				break;
			}
			needUpdate = true;
			if (old_state == SimulateWarActivityState.SW_NOT_OPEN) {
				old_state = SimulateWarActivityState.SW_SHOW;
				activityInfo.setState(old_state);
				onShowStart();
			} else if (old_state == SimulateWarActivityState.SW_SHOW) {
				old_state = SimulateWarActivityState.SW_SIGN_UP;
				activityInfo.setState(old_state);
				onSignStart();
			} else if (old_state == SimulateWarActivityState.SW_SIGN_UP) {
				old_state = SimulateWarActivityState.SW_MANAGE;
				activityInfo.setState(old_state);
				onManageStart();
			} else if (old_state == SimulateWarActivityState.SW_MANAGE) {
				old_state = SimulateWarActivityState.SW_MARCH;
				activityInfo.setState(old_state);
				onMarchStart();
			} else if (old_state == SimulateWarActivityState.SW_MARCH) {
				old_state = SimulateWarActivityState.SW_FIGHT_SHOW;
				activityInfo.setState(old_state);
				onFightShowStart();
			} else if (old_state == SimulateWarActivityState.SW_FIGHT_SHOW) {
				old_state = SimulateWarActivityState.SW_REWARD;
				activityInfo.setState(old_state);
				onReward();
			} else if (old_state == SimulateWarActivityState.SW_REWARD) {
				old_state = SimulateWarActivityState.SW_HIDDEN;
				activityInfo.setState(old_state);
				onHidden();
			} 
		}

		if (needUpdate) {
			activityInfo = newInfo;
			activityInfo.saveToRedis();
			// 在线玩家推送活动状态  起服的时候synPageInfo会有问题，但是因为没有玩家在线不会进来，所以无视.
			for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
				syncPageInfo(player);
			}
			logger.info("simulate war state change, oldTerm: {}, oldState: {} ,newTerm: {}, newState: {}", old_term, old_state, activityInfo.getTermId(),
					activityInfo.getState());
		}

	}
	
	/**
	 * 发奖的时候把match信息再加载一下.
	 */
	private void onReward() {
		//尝试加载一下即可.
		this.loadMatchInfo(true);
		//发奖.
		this.doReward();
	}	
	
	protected void doReward() {		
		int termId = this.activityInfo.getTermId();
		Map<String, SimulateWarMatchInfo> matchMap = this.guildMatchMap;
		for (Entry<String, SimulateWarMatchInfo> entry : matchMap.entrySet()) {
			try {
				String guildId = entry.getKey();
				SimulateWarMatchInfo matchInfo = entry.getValue();
				//如果完成了发奖
				if (entry.getValue().isReward()) {
					continue;
				}
				
				//只发本服.
				GuildInfoObject guildObj = GuildService.getInstance().getGuildInfoObject(guildId);
				if (guildObj == null) {
					continue;
				}					
				
				SimulateWarGuildBattle.Builder battleData = RedisProxy.getInstance().getSimulateWarGuildBattle(termId, guildId);
				if (battleData == null) {
					logger.error("not found battle data guildId:{}", guildId);
					
					continue;
				} 
				
				SimulateWarGuildInfo guildInfo = null;
				if (battleData.hasGuildA() && battleData.getGuildA().getId().equals(guildId)) {
					guildInfo = battleData.getGuildA();				
				} else if (battleData.hasGuildB() && battleData.getGuildB().getId().equals(guildId)){
					guildInfo = battleData.getGuildB();
				}
				
				//到了这里就设置发奖状态了.
				matchInfo.finishReward();
				RedisProxy.getInstance().addOrUpdateSimulateWarMatchInfo(termId, guildId, JSON.toJSONString(matchInfo));
				
				if (guildInfo == null) {
					logger.error("not found guild data ");
					
					continue;
				}					
				//每次只处理一个公会的发奖, 因为这里的发奖可能涉及到玩家的 makesure.
				sendGuildReward(termId, guildInfo.getId(), guildInfo.getWinCount());
			} catch (Exception e) {
				HawkException.catchException(e);
				logger.error("send guild:{} reward errro:",  entry.getKey());
			}						 	
		}
	}
	
	/**
	 * 减少工会的出战队列.
	 * @param guildData
	 * @param teamNum
	 * @param battleValue
	 */
	public void subGuildTeamNum(int termId, SimulateWarGuildData guildData, int teamNum, long battleValue) {
		guildData.addTeamNum(-teamNum);
		guildData.addBattleValue(-battleValue);
		
		if (guildData.getTeamNum() <= 0) {
			//删除个工会的报名信息.
			this.removeGuildData(guildData.getGuildId());
			RedisProxy.getInstance().deleteSimulateWarGuildData(termId, guildData.getGuildId());			
		} else {
			RedisProxy.getInstance().addOrUpdateSimulateWarGuildData(termId, guildData, this.getKeyExpireTime());
		}
	}
	
	/**
	 * 给公会发奖 
	 * 我估摸着这里要显示一下公会名字.
	 */
	private void sendGuildReward(int termId, String guildId, int winCount) {
		logger.info("send simulate war award termId:{}, guildId:{}, winCount:{}", termId, guildId, winCount);
		Collection<String> playerIdCollection = RedisProxy.getInstance().getSimulateWarBattlePlayer(termId, guildId);
		if (CollectionUtils.isEmpty(playerIdCollection)) {
			logger.info("can not found playerIdCollection guildId:{} winCount:{}", guildId, winCount);
			
			return;
		}
		
		SimulateWarRewarCfg cfg = this.getRewardCfg(winCount);
		for(String playerId : playerIdCollection) {
			/*String nowGuildId = GuildService.getInstance().getPlayerGuildId(playerId);
			//工会不一样了,不发奖.
			if (HawkOSOperator.isEmptyString(nowGuildId) || !nowGuildId.equals(guildId)) {
				logger.info("player :{} change guildId sign guildId:{} now guildId:{} ", playerId, guildId, nowGuildId);
				continue;
			}*/
			MailParames.Builder builder = MailParames.newBuilder().setPlayerId(playerId).setMailId(cfg.getRewardMailId()).setRewards(cfg.getAwardList());
			builder.setAwardStatus(MailRewardStatus.NOT_GET);
			SystemMailService.getInstance().sendMail(builder.build());
		} 
	}
	
	private SimulateWarRewarCfg getRewardCfg(int winCount) {
		ConfigIterator<SimulateWarRewarCfg> iter = HawkConfigManager.getInstance().getConfigIterator(SimulateWarRewarCfg.class);
		Optional<SimulateWarRewarCfg> option = iter.stream().filter(cfg->cfg.getWinCount() == winCount).findAny();
		
		return option.get(); 
	}

	private void onHidden() {
		reinitMemorInfo();
		
	}

	private void onFightShowStart() {
		
	}

	private void onMarchStart() {
		loadAllEnemyGuildData(true);
		this.loadMatchInfo(true);
	}

	private void onManageStart() {		
	}

	private void onSignStart() {
		reinitMemorInfo();		
	}

	private void onShowStart() {		
		reinitMemorInfo();
	}
	
	private void reinitMemorInfo() {
		this.guildData = new ConcurrentHashMap<>();
		this.playerDataMap = new ConcurrentHashMap<>();
		this.guildWayMap = new ConcurrentHashMap<>();
		this.enemyGuildData = new ConcurrentHashMap<>();
		this.guildMatchMap = new ConcurrentHashMap<>();
		this.guildBattleRecordMap = new ConcurrentHashMap<>();
	}
	
	/**
	 * 加载本地的报名信息.
	 * @param force
	 */
	private void loadLocalSignGuildData(boolean force) {
		if (force ) {			
			Map<String, SimulateWarGuildData> map = RedisProxy.getInstance().getSimulateWarAllGuildData(this.activityInfo.getTermId());			
			Map<String, SimulateWarGuildData> tmpMap = new ConcurrentHashMap<>();
			for (SimulateWarGuildData swgd : map.values()) {
				if (GuildService.getInstance().getGuildInfoObject(swgd.getGuildId()) != null) {
					tmpMap.put(swgd.getGuildId(), swgd);
				}
			}			
			this.guildData = tmpMap;
		} else {
			if (!guildData.isEmpty()) {
				Map<String, SimulateWarGuildData> map = RedisProxy.getInstance().getSimulateWarAllGuildData(this.activityInfo.getTermId());
				for (SimulateWarGuildData swgd : map.values()) {
					if (GuildService.getInstance().getGuildInfoObject(swgd.getGuildId()) != null) {
						this.guildData.put(swgd.getGuildId(), swgd);
					}
				}					
			}
		}
	}
	private void loadAllEnemyGuildData(boolean force) {
		if (force || this.enemyGuildData.isEmpty()) {
			this.enemyGuildData = new ConcurrentHashMap<>();
			Map<String, SimulateWarGuildData> map = RedisProxy.getInstance().getSimulateWarAllGuildData(this.activityInfo.getTermId());
			this.enemyGuildData.putAll(map);
		}
		
	}
	/**
	 * 每个服flush自己的报名区服到match 队列.
	 */
	public void flushSignGuildToMatch() {
		//判断是否已经刷进去了.
		if (activityInfo.hasFinishState(SimulateWarActivityData.FLUSH_SIGN)) {
			return;
		}
		Map<String, SimulateWarGuildData> map = this.guildData;
		int termId = this.activityInfo.getTermId();
		int keyExpireTime = this.getKeyExpireTime();				
		//公会
		Map<String, String> guidlIdServerIdMap = new HashMap<>();
		String serverId = GsConfig.getInstance().getServerId();		
		for (Entry<String, SimulateWarGuildData> entry : map.entrySet()) {
			boolean isOk = flushSignGuildToMatch(entry.getKey(), entry.getValue());
			if (isOk) {
				guidlIdServerIdMap.put(entry.getKey(), serverId);
			}
		}
		
		//只有非空才可以设置.
		if (!guidlIdServerIdMap.isEmpty()) {
			//这里的公会数量应该不会很多.
			RedisProxy.getInstance().addOrUpdateSimulateWarMatchGuildId(termId, guidlIdServerIdMap, keyExpireTime);
		}
		
		activityInfo.finishState(SimulateWarActivityData.FLUSH_SIGN);
		activityInfo.saveToRedis();
	}
	
	private boolean flushSignGuildToMatch(String guildId, SimulateWarGuildData guildData) {
		int termId = this.activityInfo.getTermId();
		int keyExpireTime = this.getKeyExpireTime();
		SimulateWarConstCfg constCfg = SimulateWarConstCfg.getInstance();
		try {
			//这边做一个公会校验.
			boolean exist = GuildService.getInstance().getGuildInfoObject(guildId) != null;
			if (!exist) {
				logger.info("guildId:{} not found", guildId);
				
				return false;
			}
			
			//检测该工会报名的队伍是否小于最小的报组的次数.
			if (guildData.getTeamNum() < constCfg.getMinTeamNum()) {
				logger.info("guildId:{} team:{} less than min tean", guildId, guildData.getTeamNum());
				
				sendNotJoinMatchMail(guildId);
				
				return false;
			}
			
			//没有出兵信息
			Map<WayType, List<String>> wayMap = this.getGuildWayMarch(guildId);
			if (MapUtils.isEmpty(wayMap)) {
				logger.info("guildId:{} wayMap is empty", guildId);
				
				return false;
			}			
			
			Map<WayType, List<String>> subWayMap = new HashMap<>();
			Set<String> playerIdSet = new HashSet<>();
			for (Entry<WayType, List<String>>  wayEntry: wayMap.entrySet()) {
				List<String> marchIdList = wayEntry.getValue();
				if (CollectionUtils.isEmpty(marchIdList)) {
					continue;
				}
												
				List<PBSimulateWarBattleData> battleDataList = new ArrayList<>();
				//根据marchId取到玩家信息.
				String playerId = null;
				for (String marchId : marchIdList) {
					playerId = this.getPlayerIdByMarchId(marchId);
					playerIdSet.add(playerId);
					PBSimulateWarPlayer.Builder warPlayer = this.getPBSimulateWarPlayer(playerId);
					if (warPlayer == null) {
						logger.error("in guildWayMarch but not in warPlayerMap playerId:{}, marchId:{}", playerId, marchId);
						
						continue;
					}
					
					for (PBSimulateWarBattleData battleData : warPlayer.getBattleDatasList()) {
						if (battleData.getId().equals(marchId)) {
							battleDataList.add(battleData);
							
							break;
						}
					}
					
											
				}
				
				Collections.sort(battleDataList, new SimulateWarMarchCompartor());
				List<String> subList = new ArrayList<>();
				List<PBSimulateWarBattleData> deleteList = new ArrayList<>();
				for (PBSimulateWarBattleData battleData : battleDataList) {
					if (subList.size() >= constCfg.getMemberLimit()) {
						deleteList.add(battleData);
					} else {
						subList.add(battleData.getId());
					}
				}
				
				subWayMap.put(wayEntry.getKey(), subList);
				for (PBSimulateWarBattleData deleteBattleData : deleteList) {
					playerId = this.getPlayerIdByMarchId(deleteBattleData.getId());					
					PBSimulateWarPlayer.Builder warPlayer = this.getPBSimulateWarPlayer(playerId);
					if (warPlayer == null) {
						logger.error("in guildWayMarch but not in warPlayerMap playerId:{}, marchId:{}", playerId, deleteBattleData.getId());
						
						continue;
					}
					
					int index = - 1;
					for (int j = 0; j < warPlayer.getBattleDatasCount(); j ++) {
						PBSimulateWarBattleData battleData = warPlayer.getBattleDatas(j);
						if (battleData.getId().equals(deleteBattleData.getId())) {
							index = j;
							
							break;
						}
					}
					
					if (index >= 0) {
						warPlayer.removeBattleDatas(index);
						if (warPlayer.getBattleDatasCount() <= 0) {
							//一路出战斗没有的玩家从该列表里面清楚.
							playerIdSet.remove(playerId);
							RedisProxy.getInstance().deleteSimualteWarGuildPlayer(termId, guildId, playerId);						
						} else {
							RedisProxy.getInstance().addOrUpdateSimulateWarGuildPlayer(termId, guildId, warPlayer, keyExpireTime);
						}																	
					}									
				}
				
				if (deleteList.size() > 0) {
					guildData.addTeamNum(- deleteList.size());
					RedisProxy.getInstance().addOrUpdateSimulateWarGuildData(termId, guildData, keyExpireTime);
				}
			}
			
			//这里把所有路的信息都填满.
			EnumSet<WayType> enumWaySet = EnumSet.allOf(WayType.class);
			for (WayType wayType : enumWaySet) {
				List<String> wayList = subWayMap.get(wayType);
				if (wayList == null) {
					subWayMap.put(wayType, new ArrayList<>());
				}
			}
			
			//记录一个出战玩家.
			RedisProxy.getInstance().addOrUpdateSimulateWarBattlePlayer(termId, guildId, playerIdSet, keyExpireTime);
			
			this.setGuildWayMarch(guildId, subWayMap);
			RedisProxy.getInstance().addOrUpdateSimulateWarWayMarch(termId, guildId, subWayMap, keyExpireTime);
			
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return false;
	}
	
	//发送无法参与匹配的邮件.
	private void sendNotJoinMatchMail(String guildId) {
		Map<WayType, List<String>> wayMap = this.getGuildWayMarch(guildId);
		if (MapUtils.isEmpty(wayMap)) {
			logger.info("guildId:{} wayMap is empty", guildId);
			
			return;
		}
		
		Set<String> playerIdSet = new HashSet<>();
		for (List<String> marchIdList : wayMap.values()) {
			for (String marchId : marchIdList) {
				String playerId = this.getPlayerIdByMarchId(marchId);
				playerIdSet.add(playerId);
			}
		}
		
		for (String playerId : playerIdSet) {
			MailParames.Builder builder = MailParames.newBuilder().setPlayerId(playerId).setMailId(MailId.SIMULATE_WAR_CAN_NOT_JOIN_MATCH);			
			SystemMailService.getInstance().sendMail(builder.build());
		}
	}
	/**
	 * 当前阶段状态计算,仅供状态检测调用 
	 * 
	 * @return
	 */
	private SimulateWarActivityData calcInfo() {
		SimulateWarActivityData info = new SimulateWarActivityData();
		if (SimulateWarConstCfg.getInstance().isSystemClose() || GsConfig.getInstance().getServerType() != 0) {
			info.setState(SimulateWarActivityState.SW_HIDDEN);
			
			return info;
		}
		ConfigIterator<SimulateWarTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(SimulateWarTimeCfg.class);
		long now = HawkTime.getMillisecond();

		String serverId = GsConfig.getInstance().getServerId();
		Long mergeTime =  AssembleDataManager.getInstance().getServerMergeTime(serverId);

		long serverOpenAm0 = HawkTime.getAM0Date(new Date(GameUtil.getServerOpenTime())).getTime();
		long serverDelay = SimulateWarConstCfg.getInstance().getServerDelay();
		SimulateWarTimeCfg cfg = null;
		for (SimulateWarTimeCfg timeCfg : its) {
			List<String> limitServerLimit = timeCfg.getLimitServerList();
			List<String> forbidServerLimit = timeCfg.getForbidServerList();
			
			// 开服时间不满足 
			if (serverOpenAm0 + serverDelay > timeCfg.getShowTimeValue()) {
				continue;
			}
			
			// 合服时间在活动期间内,则不参与攻防模拟战.
			if (mergeTime != null && mergeTime >= timeCfg.getShowTimeValue() && mergeTime <= timeCfg.getHiddenTimeValue()) {
				continue;
			}
			
			// 开启判定,如果没有开启区服限制,或者本期允许本服所在区组开放
			if ((limitServerLimit.isEmpty() || limitServerLimit.contains(serverId)) && (forbidServerLimit == null || !forbidServerLimit.contains(serverId))) {
				
				if (now > timeCfg.getShowTimeValue()) {
					cfg = timeCfg;
				}
			}
		}

		// 没有可供开启的配置
		if (cfg == null) {
			return info;
		}

		int termId = 0;
		SimulateWarActivityState state = SimulateWarActivityState.SW_HIDDEN;
		if (cfg != null) {
			termId = cfg.getTermId();
			long showStartTime = cfg.getShowTimeValue();
			long signStartTime = cfg.getSignUpStartTimeValue();
			long manageStartTime = cfg.getManageStartTimeValue();
			long marchStartTime = cfg.getMarchStartTimeValue();
			long fighterShowTime = cfg.getFightShowStartTimeValue();
			long hiddenTime = cfg.getHiddenTimeValue();
			long rewardTime = cfg.getRewardTimeValue();
			if (now < showStartTime) {
				state = SimulateWarActivityState.SW_NOT_OPEN;
			} else if (now >= showStartTime && now < signStartTime) {
				state = SimulateWarActivityState.SW_SHOW;
			} else if (now >= signStartTime && now < manageStartTime) {
				state = SimulateWarActivityState.SW_SIGN_UP;
			} else if (now >= manageStartTime && now < marchStartTime) {
				state = SimulateWarActivityState.SW_MANAGE;
			} else if (now >= marchStartTime && now < fighterShowTime) {
				state = SimulateWarActivityState.SW_MARCH;
			} else if (now >= fighterShowTime && now < rewardTime) {
				state = SimulateWarActivityState.SW_FIGHT_SHOW;
			} else if (now >= rewardTime && now < hiddenTime) {
				state = SimulateWarActivityState.SW_REWARD;
			} else if (now >= hiddenTime) {
				state = SimulateWarActivityState.SW_HIDDEN;
			}
		}

		info.setTermId(termId);
		info.setState(state);
		return info;
	}
	
	
	/**
	 * 针对每条路生成一个marchId
	 * @param playerId
	 * @param num
	 * @return
	 */
	public String generateMarchhId(String playerId, int num) {
		return playerId  + "_" + num;
	}
	
	/**
	 * 根据marchId获得playerId
	 * @param marchId
	 * @return 
	 */
	public String getPlayerIdByMarchId(String marchId) {
		return marchId.split("_")[0];
	}
	
	/**
	 * 同步锦标赛活动状态
	 * 
	 * @param player
	 */
	public void syncPageInfo(Player player) {
		SimulateWarPageInfoResp.Builder stateInfo = genPageInfo(player.getId());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.SIMULATE_WAR_PAGE_INFO_RESP_VALUE, stateInfo));
	}

	private SimulateWarPageInfoResp.Builder genPageInfo(String playerId) {
		SimulateWarPageInfoResp.Builder builder = SimulateWarPageInfoResp.newBuilder();
		SimulateWarActivityStateInfo.Builder stateInfo = genStateInfo(playerId);
		String guildId = GuildService.getInstance().getPlayerGuildId(playerId);
		boolean hasGuild = !HawkOSOperator.isEmptyString(guildId);			
		SimulateWarConstCfg constCfg = SimulateWarConstCfg.getInstance();
		
		if (hasGuild) {
			SimulateWarGuildData guildData = this.getGuildDat(guildId);
			Map<WayType, List<String>> wayMap = this.getGuildWayMarch(guildId);
			switch(activityInfo.getState()) {
			case SW_SIGN_UP:
				if (guildData != null) {
					builder.setTeamTotalCount(guildData.getTeamNum());
				}
				//分路信息
				if (wayMap != null) {
					for (Entry<WayType, List<String>> entry : wayMap.entrySet()) {
						WayNumInfo.Builder wayNumInfo = WayNumInfo.newBuilder();
						wayNumInfo.setWay(entry.getKey());
						wayNumInfo.setNum(entry.getValue().size());
						builder.addNumInfo(wayNumInfo);
					}
				}	
				break;
			case SW_MANAGE:
			case SW_MARCH:
			case SW_FIGHT_SHOW:
				//这里的逻辑写的相当混乱.需求来回弄.
				//如果人数小于多少人就直接隐藏界面.
				if (guildData == null || guildData.getTeamNum() < constCfg.getMinTeamNum()) {
					stateInfo.setState(SimulateWarActivityState.SW_NOT_OPEN);
					break;
				}
				if (guildData != null) {
					builder.setTeamTotalCount(guildData.getTeamNum());
					builder.setEncourageTimes(guildData.getEncourageTimes());					
				}
				//分路信息
				if (wayMap != null) {
					for (Entry<WayType, List<String>> entry : wayMap.entrySet()) {
						WayNumInfo.Builder wayNumInfo = WayNumInfo.newBuilder();
						wayNumInfo.setWay(entry.getKey());
						wayNumInfo.setNum(entry.getValue().size());
						builder.addNumInfo(wayNumInfo);
					}
				}	
				//行军阶段显示敌对联盟的一些信息.
				if (activityInfo.getState() == SimulateWarActivityState.SW_MARCH || activityInfo.getState() == SimulateWarActivityState.SW_FIGHT_SHOW ) {
					SimulateWarMatchInfo matchInfo = this.getMatchInfo(guildId);
					if (matchInfo != null) {
						String enemyGuildId = matchInfo.getEnemyGuildId(guildId);
						if (!HawkOSOperator.isEmptyString(enemyGuildId)) {
							SimulateWarGuildData enemyGuildData = this.getEnemyGuildData(enemyGuildId);
							if (enemyGuildData != null) {
								builder.setEnemyEncourageTimes(enemyGuildData.getEncourageTimes());
								builder.setEnemyTeamTotalCount(enemyGuildData.getTeamNum());
								builder.setEnemyGuildName(enemyGuildData.getGuildName());
								builder.setEnemyGuildServerId(matchInfo.getServerId(enemyGuildData.getGuildId()));
							}
						}
					}
				}
				
				//战斗展示阶段把奖励信息展示一下。
				if (activityInfo.getState() == SimulateWarActivityState.SW_FIGHT_SHOW  ) {
					SimulateWarGuildBattle.Builder battleBuilder = RedisProxy.getInstance().getSimulateWarGuildBattle(activityInfo.getTermId(), guildId);
					if (battleBuilder != null) {
						builder.setBattleInfo(battleBuilder);					}
				}
				break;
			case SW_REWARD:
				//如果人数小于多少人就直接隐藏界面.
				if (guildData == null || guildData.getTeamNum() < constCfg.getMinTeamNum()) {
					stateInfo.setState(SimulateWarActivityState.SW_NOT_OPEN);
				}
				
				SimulateWarMatchInfo matchInfo = this.getMatchInfo(guildId);
				if (matchInfo != null) {
					String enemyGuildId = matchInfo.getEnemyGuildId(guildId);
					if (!HawkOSOperator.isEmptyString(enemyGuildId)) {
						SimulateWarGuildData enemyGuildData = this.getEnemyGuildData(enemyGuildId);
						if (enemyGuildData != null) {
							builder.setEnemyEncourageTimes(enemyGuildData.getEncourageTimes());
							builder.setEnemyTeamTotalCount(enemyGuildData.getTeamNum());
							builder.setEnemyGuildName(enemyGuildData.getGuildName());
							builder.setEnemyGuildServerId(matchInfo.getServerId(enemyGuildData.getGuildId()));
						}
					}
				}
				
				SimulateWarGuildBattle.Builder battleBuilder = RedisProxy.getInstance().getSimulateWarGuildBattle(activityInfo.getTermId(), guildId);
				if (battleBuilder != null) {
					builder.setBattleInfo(battleBuilder);					
				}				
				break;
			default:
				break;
			}						
			
		} else {
			stateInfo.setState(SimulateWarActivityState.SW_NOT_OPEN);
		}
		
	
		builder.setStateInfo(stateInfo);
		return builder;
	}
	
	public SimulateWarActivityStateInfo.Builder genStateInfo(String playerId) {
		SimulateWarActivityStateInfo.Builder builder = SimulateWarActivityStateInfo.newBuilder();
		SimulateWarActivityState state = activityInfo.getState();
		int termId = activityInfo.getTermId();
		builder.setStage(termId);
		builder.setState(state);
		SimulateWarTimeCfg cfg = activityInfo.getTimeCfg();
		if (cfg != null) {
			builder.setShowStartTime(cfg.getShowTimeValue());
			builder.setSignStartTime(cfg.getSignUpStartTimeValue());
			builder.setMangeStarTime(cfg.getManageStartTimeValue());
			builder.setMarchStartTime(cfg.getMarchStartTimeValue());
			builder.setFightShowStarTime(cfg.getFightShowStartTimeValue());
			builder.setRewardStartTime(cfg.getRewardTimeValue());
			builder.setHiddenTime(cfg.getHiddenTimeValue());			
			builder.setNewlyTime(HawkTime.getAM0Date(new Date(cfg.getShowTimeValue())).getTime() + GsConst.DAY_MILLI_SECONDS);
		}
		return builder;
	}
	
	/**
	 * 出征.
	 * @param player
	 * @param req
	 * @return
	 */
	@MessageHandler
	public void saveMarchPlayer(SimulateWarSaveMarchMsg msg) {
		Player player = msg.getPlayer(); 		
		int errorCode = saveMarchPlayer(player, msg.getBattleData(), msg.getArmyMap());
		if (errorCode != Status.SysError.SUCCESS_OK_VALUE) {
			player.sendError(HP.code.SIMULATE_WAR_SIGN_UP_REQ_VALUE, errorCode, 0);			
		} else {
			player.responseSuccess(HP.code.SIMULATE_WAR_SIGN_UP_REQ_VALUE);
		}
	}
	
	private SimulateWarGuildData newSimulateWarGuildData(GuildInfoObject guildObj) {
		SimulateWarGuildData guildData = new SimulateWarGuildData();
		guildData.setGuildId(guildObj.getId());
		guildData.setGuildTag(guildObj.getTag() == null ? "" : guildObj.getTag());
		guildData.setGuildName(guildObj.getName());
		guildData.setGuildFlag(guildObj.getFlagId());
		
		return guildData;
	}
	/**
	 * 保存出兵.
	 * @param player
	 * @param req
	 * @param wayType
	 * @param wayBattleData
	 * @param armyMap
	 * @return
	 */
	private int saveMarchPlayer(Player player, PBSimulateWarBattleData.Builder wayBattleData, Map<Integer, Integer> armyMap) {		
		logger.info("playerId:{} signOrAdjust, wayType:{}", player.getId(), wayBattleData.getWay() == null ? "NULL" : wayBattleData.getWay() );
		int termId = activityInfo.getTermId();	
		String guildId = player.getGuildId();
		SimulateWarConstCfg constCfg = SimulateWarConstCfg.getInstance(); 
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
		if (guild == null) {
			logger.warn("guild:{} not found ", guildId);
			
			return Status.Error.GUILD_NO_JOIN_VALUE;
		}
				
						
		PBSimulateWarPlayer.Builder warPlayer = this.getPBSimulateWarPlayer(player.getId());
		if (warPlayer == null) {
			warPlayer = PBSimulateWarPlayer.newBuilder();			
		}
		
		warPlayer.setPlayerInfo(BuilderUtil.buildSimulateWarBasePlayerStruct(player));
		String wayId = null;
		boolean generateId = false;
		//Map<Integer, Integer> oldArmyMap = new HashMap<>();
		PBSimulateWarBattleData.Builder oldWayBattleData = null; 
		for (PBSimulateWarBattleData.Builder battleData : warPlayer.getBattleDatasBuilderList()) {
			if (wayBattleData.hasId() && wayBattleData.getId().equals(battleData.getId())) {
				oldWayBattleData = battleData;												
			} else {
				
				/*battleData.getSoldiersList().stream().forEach(soldier->{
					MapUtil.appendIntValue(oldArmyMap, soldier.getArmyId(), soldier.getCount());
				});*/
				//检测英雄是否有重复.
				for (PBHeroInfo.Builder heroInfo : battleData.getHerosBuilderList()) {
					for (PBHeroInfo.Builder newHeroInfo : wayBattleData.getHerosBuilderList()) {
						if (heroInfo.getHeroId() == newHeroInfo.getHeroId()) {
							return Status.Error.SIMULATE_WAR_DUMPLICATE_HERO_VALUE;						
						}
					} 
				}
			}
		}
		
		//找不到相应的出征信息.
		if (wayBattleData.hasId() && oldWayBattleData == null) {
			return Status.Error.SIMULATE_WAR_NOT_FOUND_MARCH_VALUE;
		}
		
		/*wayBattleData.getSoldiersBuilderList().forEach(armyBuilder->{
			MapUtil.appendIntValue(oldArmyMap, armyBuilder.getArmyId(), armyBuilder.getCount());
		});*/
		
		/*//检测
		for (Entry<Integer, Integer> entry : armyMap.entrySet()) {
			//三路兵的总数
			int marchCount = oldArmyMap.getOrDefault(entry.getKey(), 0);
			if (marchCount > entry.getValue()) {
				logger.info("playerId:{} arm not enouge armyId:{} marchCount:{}, totalCount:{}", player.getId(), entry.getKey(), marchCount, entry.getValue());
				
				return Status.Error.SIMULATE_WAR_ARMY_CNT_NOT_ENOUGH_VALUE;
			}
		}*/
		
		SimulateWarGuildData guildData = this.getGuildDat(guildId);
		if (guildData == null) {
			guildData = newSimulateWarGuildData(guild);			
		}
		
		long battleValueChange = 0;
		if (oldWayBattleData == null) {
			//队伍达到最大了.
			if (warPlayer.getBattleDatasCount() >= constCfg.getPersonMaxTeam()) {
				return Status.Error.SIMULATE_WAR_PERSON_MAX_TEAM_VALUE;
			}
			battleValueChange = wayBattleData.getBattleValue();
			guildData.addTeamNum(1);
			String marchId = this.generateMarchhId(player.getId(), warPlayer.getOrderNum() + 1);
			wayBattleData.setId(marchId);
			wayId = marchId;
			warPlayer.addBattleDatas(wayBattleData);
			generateId = true;
			//只有新出征的才算.
			LogUtil.logSimulateWarSign(player, termId, guildId, wayBattleData.getWay(), GsConst.SimulateWarConst.SIGN_UP);
		} else {
			//则把老数据拷贝新的结构体上面.
			battleValueChange = wayBattleData.getBattleValue() - oldWayBattleData.getBattleValue();
			wayId = oldWayBattleData.getId();
			WayType oldWayType = oldWayBattleData.getWay();
			wayBattleData.setId(wayId);
			wayBattleData.setWay(oldWayType);
			oldWayBattleData.clear();			
			oldWayBattleData.mergeFrom(wayBattleData.build());			
		}
		guildData.addBattleValue(battleValueChange);		
											
		this.addGuildWayMarch(guildId, wayBattleData.getWay(), wayId);		
		if (generateId) {
			warPlayer.setOrderNum(warPlayer.getOrderNum() + 1);
		}
		
		//在这里才放到缓存里面去.
		this.setPBSimulateWarPlayer(player.getId(), warPlayer);	
		this.addGuildData(guildData);
		RedisProxy.getInstance().addOrUpdateSimulateWarGuildPlayer(termId, guildId, warPlayer, this.getKeyExpireTime());
		RedisProxy.getInstance().addOrUpdateSimulateWarGuildData(termId, guildData, this.getKeyExpireTime());
				
		this.synPlayerAllMarchInfo(player);
		this.synSimulateWarWayPlayerReq(player, guildId, wayBattleData.getWay());
		this.syanPageInfoUpdate(player);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
		
	
	/**
	 * key 的过期时间.
	 * @return
	 */
	private int getKeyExpireTime() {
		SimulateWarTimeCfg timeCfg = this.activityInfo.getTimeCfg();
		if (timeCfg == null) {
			return 86400 * 15;
		} else {
			return ((int)((timeCfg.getHiddenTimeValue() - HawkTime.getMillisecond()) / 1000)) + 86400 * 15;
		}
	}

	public void setPBSimulateWarPlayer(String playerId, PBSimulateWarPlayer.Builder pbBuilder) {
		this.playerDataMap.put(playerId, pbBuilder);
	}
	
	public PBSimulateWarPlayer.Builder getPBSimulateWarPlayer(String playerId) {
		return this.playerDataMap.get(playerId);
	}
	
	public PBSimulateWarPlayer.Builder removePBSimulateWarPlayer(String playerId) {
		return this.playerDataMap.remove(playerId); 
	}
	
	/**
	 * 获取报名的公会信息.
	 * @param guildId
	 * @return
	 */
	public SimulateWarGuildData getGuildDat(String guildId) {
		return this.guildData.get(guildId);
	}
	
	/**
	 * 删除报名的工会信息.
	 * @param guildId
	 */
	public void removeGuildData(String guildId) {
		this.guildData.remove(guildId);
	}
	
	/**
	 * 添加工会的报名信息.
	 * @param guildData
	 */
	public void addGuildData(SimulateWarGuildData guildData) {
		this.guildData.put(guildData.getGuildId(), guildData);
	}
	/**
	 * 获取工会的分路出兵信息
	 * @param guildId
	 * @return
	 */
	public Map<WayType, List<String>> getGuildWayMarch(String guildId) {
		return this.guildWayMap.get(guildId);
	}
	
	/**
	 * 设置
	 * @param guildId
	 * @param map
	 */
	public void setGuildWayMarch(String guildId, Map<WayType, List<String>> map) {
		this.guildWayMap.put(guildId, map);
	}
	
	/**
	 * 某路增加.
	 * @param guildId
	 * @param wayType
	 * @param id
	 */
	public void addGuildWayMarch(String guildId, WayType wayType, String id) {
		Map<WayType, List<String>> map = this.guildWayMap.get(guildId);
		if (map == null) {
			map = new ConcurrentHashMap<WayType, List<String>>();
			this.guildWayMap.put(guildId, map);
		}
		
		List<String> idList = map.get(wayType);
		if (idList == null) {
			idList = new ArrayList<>();
			map.put(wayType, idList);
		} 
		if (!idList.contains(id)) {
			idList.add(id);
			RedisProxy.getInstance().addOrUpdateSimulateWarWayMarch(activityInfo.getTermId(), guildId, map, this.getKeyExpireTime());
		}						
	}
	
	/**
	 * 删除某路的行军.
	 * @param guildId
	 * @param wayType
	 * @param marchId
	 */
	public void removeGuildWayMarch(String guildId, WayType wayType, String marchId) {
		Map<WayType, List<String>> map = this.guildWayMap.get(guildId);
		if (map == null) {
			logger.error("can not found guild:{} info", guildId);
			
			return;
		}
		
		List<String> idList = map.get(wayType);
		if (idList == null) {
			logger.error("can not found guild:{} wayType:{} way march", guildId, wayType);
			
			return;
		} 
		
		if (idList.remove(marchId)) {			
			RedisProxy.getInstance().addOrUpdateSimulateWarWayMarch(activityInfo.getTermId(), guildId, map, this.getKeyExpireTime());
		} else {
			logger.error("can not found guildId:{} wayType:{} marchId:{}", guildId, wayType, marchId);
		}						
	}
	
	@MessageHandler
	private void onEncouregeMsg(SimulateWarEncourageMsg msg) {
		logger.info("encourge guildId:{}", msg.getGuildId());
		SimulateWarGuildData guildData = this.getGuildDat(msg.getGuildId());
		if (guildData == null) {
			logger.info("guildData is null guildId:{}", msg.getGuildId());
						
			return;
		}
		
		SimulateWarConstCfg constCfg = SimulateWarConstCfg.getInstance(); 
		if (guildData.getEncourageTimes() >= constCfg.getMaxEncourageTimes()) {
			return;
		}			
		
		guildData.setEncourageTimes(guildData.getEncourageTimes() + 1);
		RedisProxy.getInstance().addOrUpdateSimulateWarGuildData(this.activityInfo.getTermId(), guildData, this.getKeyExpireTime());
	}  
	
	public void synEncourageInfo(Player player) {
		int termId = this.activityInfo.getTermId();
		SimulateWarPlayerExt playerExt = RedisProxy.getInstance().getSimulateWarPlayerExt(termId, player.getId());
		SimulateWarGuildData simulateWarGuildData = this.getGuildDat(player.getGuildId());
		int totalEncourageTimes = 0;
		if (simulateWarGuildData != null) {
			totalEncourageTimes = simulateWarGuildData.getEncourageTimes();
		}
		this.synEncourgeInfo(player, playerExt.getEncourageTimes(), totalEncourageTimes);
	}
	/**
	 * 整个联盟的次数.
	 * @param player
	 * @param map
	 * @param totalEncourageTimes
	 */
	public void synEncourgeInfo(Player player, int playerEncourageTimes, int totalEncourageTimes) {
		SimulateWarEncoureageInfoResp.Builder sbuilder = SimulateWarEncoureageInfoResp.newBuilder();
		sbuilder.setGuildEncourageTimes(totalEncourageTimes);
		sbuilder.setPlayerEncourageTimes(playerEncourageTimes);
		
		HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code.SIMULATE_WAR_ENCOUREAGE_INFO_RESP_VALUE, sbuilder);
		player.sendProtocol(hawkProtocol);
	}

	public SimulateWarActivityData getActivityInfo() {
		return activityInfo;
	} 
	
	/**
	 * 获取匹配信息
	 * @param guildId
	 * @return
	 */
	public SimulateWarMatchInfo getMatchInfo(String guildId) {
		return this.guildMatchMap.get(guildId);
	}
	
	/**
	 * 获取敌对联盟的相关信息.
	 * @param guildId
	 * @return
	 */
	public SimulateWarGuildData getEnemyGuildData(String guildId) {
		return enemyGuildData.get(guildId);
	}	
	
	/**
	 * 在manage 的时候tick
	 */
	private void manageTick() {
		try {
			flushSignGuildToMatch();
		} catch (Exception e) {
			HawkException.catchException(e);
		}		
		
		try {
			matchOnManageState();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
	}
	
	/**
	 * 在march 的时候tick.
	 */
	private void marchTick() {
		doBattleOnMarchState();
	} 
	
	private void doBattleOnMarchState() {
		SimulateWarTimeCfg timeCfg = this.activityInfo.getTimeCfg();
		if (timeCfg == null) {
			return;
		}
		
		//加载敌对工会信息.
		this.loadAllEnemyGuildData(false);
		
		//还没到可以战斗的时间.
		long curTime = HawkTime.getMillisecond();
		if (curTime < timeCfg.getFightStartTimeValue()) {
			return;
		}
		
		
		//是否已经完成了战斗数据加载.
		if (this.activityInfo.hasFinishState(SimulateWarActivityData.FIGHTER_FINISH)) {
			return;
		}
		
		//不管是计算战斗还是加载数据，都必须先把匹配数据加载出来.
		loadMatchInfo(false);
				
		//判断数据是否已经加载完了
		boolean loadFinish = loadBattleWayInfo();
		if (loadFinish) {
			return;
		}
		
		//数据都加载完了。可以开始战了.
		boolean lastCalc = doBattle();
		if (!lastCalc) {
			this.activityInfo.finishState(SimulateWarActivityData.FIGHTER_FINISH);
			RedisProxy.getInstance().addOrUpdateSimulateWarActivityInfo(this.activityInfo);
			
			logger.info("all battle over");
		}
		
	}
	
	/**
	 * 单次只计算一个公会.
	 */
	private boolean doBattle() {		
		Map<String, SimulateWarMatchInfo> matchMap = this.guildMatchMap;
		int termId = this.activityInfo.getTermId();
		for (Entry<String, SimulateWarMatchInfo> entry : matchMap.entrySet()) {
			String guildId = entry.getKey();
			SimulateWarMatchInfo matchInfo = entry.getValue();
			//不是主服不处理.
			if (!matchInfo.isMain(guildId)) {
				continue;
			}
			if (matchInfo.isBattleFinish()) {
				continue;
			}
			//如果不是本服跳过.
			if (!GlobalData.getInstance().isLocalServer(matchInfo.getMainServerId())) {
				continue;
			}
									
			try {
				
				logger.info("do guild battle guildA:{}, GuildB:{}", matchInfo.getMainGuildId(), matchInfo.getSlaveGuildId());
				
				if (HawkOSOperator.isEmptyString(matchInfo.getSlaveGuildId())) {
					SimulateWarGuildData guildData = this.getGuildDat(matchInfo.getMainGuildId());
					doGuildBattle(termId, guildData);
				} else {
					SimulateWarGuildData guildDataA = this.getGuildDat(matchInfo.getMainGuildId());
					SimulateWarGuildData guildDataB = this.getEnemyGuildData(matchInfo.getSlaveGuildId());
					HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
						
						@Override
						public Object run() {
							doGuildBattle(termId, guildDataA, guildDataB);
							
							return null;
						}
					});					
				}								
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			
			matchInfo.finishBattle();			
			RedisProxy.getInstance().addOrUpdateSimulateWarMatchInfo(termId, matchInfo.getMainGuildId(), JSON.toJSONString(matchInfo));
			if (!HawkOSOperator.isEmptyString(matchInfo.getSlaveGuildId())) {
				//内存值也要改掉.
				SimulateWarMatchInfo slaveMatchInfo = matchMap.get(matchInfo.getSlaveGuildId());
				slaveMatchInfo.finishBattle();
				RedisProxy.getInstance().addOrUpdateSimulateWarMatchInfo(termId, matchInfo.getSlaveGuildId(), JSON.toJSONString(slaveMatchInfo));
			}
			
			return true;
		}
		
		return false;
	}
	/**
	 * 加载工会的编队信息.
	 */
	private boolean loadBattleWayInfo() {
		Map<String, SimulateWarMatchInfo> matchMap = this.guildMatchMap;
		boolean load = false;
		String serverId = GsConfig.getInstance().getServerId();
		int termId = this.activityInfo.getTermId();
		for (Entry<String, SimulateWarMatchInfo>  entry: matchMap.entrySet()) {			
			SimulateWarMatchInfo matchInfo = entry.getValue();
			//不是主服不处理,因为matchInfo有两个.
			if (matchInfo.isMain(entry.getKey())) {
				continue;
			}
			if (!serverId.equals(GlobalData.getInstance().getMainServerId(matchInfo.getMainServerId()))) {
				continue;
			}
			
			//没有匹配到
			if (HawkOSOperator.isEmptyString(matchInfo.getSlaveGuildId())) {
				continue;
			}
			
			//加载敌对联盟的信息
			Map<WayType, List<String>> memMap = this.getGuildWayMarch(matchInfo.getSlaveGuildId());
			if (MapUtils.isEmpty(memMap)) {
				load = true;
				Map<WayType, List<String>> wayMarchListMap = RedisProxy.getInstance().getSimulateWarAllWayMarch(termId, matchInfo.getSlaveGuildId());
				this.setGuildWayMarch(matchInfo.getSlaveGuildId(), wayMarchListMap);
				
				//把对战的玩家信息加载到玩家里面去.
				Map<String, PBSimulateWarPlayer.Builder> playerMap = RedisProxy.getInstance().getSimulateWarGuildAllPlayer(termId, matchInfo.getSlaveGuildId());
				this.playerDataMap.putAll(playerMap);
				
				break;
			}			
		}
		
		return load;
	}

	
	/**
	 * 尝试加载匹配信息
	 */
	private void loadMatchInfo(boolean force) {		
		if (force) {
			Map<String, SimulateWarMatchInfo> redisMatchInfo = RedisProxy.getInstance().getSimulateWarMatchInfo(this.activityInfo.getTermId());
			this.guildMatchMap = new ConcurrentHashMap<>();
			this.guildMatchMap.putAll(redisMatchInfo);
		} else {
			//有就不加载
			if (this.guildMatchMap.isEmpty()) {
				Map<String, SimulateWarMatchInfo> redisMatchInfo = RedisProxy.getInstance().getSimulateWarMatchInfo(this.activityInfo.getTermId());				
				this.guildMatchMap.putAll(redisMatchInfo);
			}
		}
						
	}

	/**
	 * 在管理的后半阶段去匹配才能保证在march阶段一进入就能看到敌对玩家.
	 */
	private void matchOnManageState() {
		SimulateWarTimeCfg timeCfg = this.activityInfo.getTimeCfg();
		if (timeCfg == null) {
			return;
		}
		//还没到可以匹配的时间.
		long curTime = HawkTime.getMillisecond();
		if (curTime < timeCfg.getMatchStartTimeValue()) {
			return;
		}
		
		//是否完成了匹配.
		if (this.activityInfo.hasFinishState(SimulateWarActivityData.MATCH_FINISH)) {
			return;
		}
		
		//从redis读取匹配状态.
		int termId = this.activityInfo.getTermId();
		boolean finish = RedisProxy.getInstance().isSimulateWarMatchFinish(termId);
		if (finish) {					
			this.activityInfo.finishState(SimulateWarActivityData.MATCH_FINISH);
			this.activityInfo.saveToRedis();
			
			return;
		}
		int curSecond = HawkTime.getSeconds();
		String lockKey = RedisProxy.SIMULATE_WAR_LOCK  + ":" + termId;
		String matchLockField = "match";
		String matchServerField = "serverId";
		SimulateWarConstCfg constCfg = SimulateWarConstCfg.getInstance(); 
		boolean getLock = RedisProxy.getInstance().getRedisSession().hSetNx(lockKey, matchLockField, curSecond + "") > 0;
		if (getLock) {
			doMatch();
			//成功完成匹配之后, 记录一下是哪个服
			RedisProxy.getInstance().getRedisSession().hSet(lockKey, matchServerField, GsConfig.getInstance().getServerId(), this.getKeyExpireTime());
		} else {
			String time = RedisProxy.getInstance().getRedisSession().hGet(lockKey, matchLockField);
			int intTime = Integer.parseInt(time);
			//删除锁之后等待下一次的轮询.
			if (curTime - constCfg.getMatchLockExpire() > intTime) {
				RedisProxy.getInstance().getRedisSession().hDel(lockKey, matchLockField);
				logger.info("simulate war match timeout serverId:{}", GsConfig.getInstance().getServerId());
				
				return;
			}
		}
					
	}
	
	//实际去做match;
	/**
	 * 最早的版本是随机匹配 所以只存储了一个公会ID和区服,
	 * 修改成按照战力来做匹配, 在不修改原来流程的基础上做修改.
	 */
	private void doMatch() {
		int termId = this.getTermId();
		int keyExpireTime = this.getKeyExpireTime();
		Map<String, String> map = RedisProxy.getInstance().getSimulateWarMatchGuildId(termId);
		if (MapUtils.isEmpty(map)) {
			logger.info("match pool is empty");
			
			return;
		}
		
		//拉取所有的工会信息. 此段代码为后续增加
		Map<String, SimulateWarGuildData> allGuildDataMap = RedisProxy.getInstance().getSimulateWarAllGuildData(termId);		
		LinkedList<SimulateWarGuildData> guildDataList = new LinkedList<>();
		//只有进入到匹配池的工会才加入进来.
		for (Entry<String, SimulateWarGuildData> entry : allGuildDataMap.entrySet()) {
			if (map.containsKey(entry.getKey())) {
				guildDataList.add(entry.getValue());
			}
		}
		
		if (guildDataList.isEmpty()) {
			logger.info("guild data list is empty");
			
			return;
		}
		//排序
		Collections.sort(guildDataList, new SimulateWarGuildDataCompartor());
		
		Map<String, String> matchMap = new HashMap<>(); 		
		Map<String, SimulateWarMatchInfo> guildMatchMap = new HashMap<>();
		SimulateWarConstCfg constCfg = SimulateWarConstCfg.getInstance();
		int count = 0;
		List<SimulateWarGuildData> tmpList = new ArrayList<>();
		for (SimulateWarGuildData guildData : guildDataList) {
			count++;
			tmpList.add(guildData);
			if (count == guildDataList.size() || tmpList.size() % constCfg.getMatching() == 0) {
				while(tmpList.size() > 0) {
					SimulateWarGuildData swgd = tmpList.remove(0);
					//为0 则落单了
					if (tmpList.size() > 0) {
						SimulateWarGuildData slaveSwgd = tmpList.remove(HawkRand.randInt(0, tmpList.size() - 1));
						SimulateWarMatchInfo swmi = new SimulateWarMatchInfo();
						swmi.setMainGuildId(swgd.getGuildId());
						swmi.setMainServerId(map.get(swgd.getGuildId()));
						swmi.setSlaveGuildId(slaveSwgd.getGuildId());
						swmi.setSlaveServerId(map.get(slaveSwgd.getGuildId()));
						
						String matchString = JSON.toJSONString(swmi);
						guildMatchMap.put(swgd.getGuildId(), swmi);
						guildMatchMap.put(slaveSwgd.getGuildId(), swmi);
						matchMap.put(swgd.getGuildId(), matchString);
						matchMap.put(slaveSwgd.getGuildId(), matchString);
					} else {
						//落单的这种情况
						if (count == guildDataList.size()) {
							SimulateWarMatchInfo swmi = new SimulateWarMatchInfo();
							swmi.setMainGuildId(swgd.getGuildId());
							swmi.setMainServerId(map.get(swgd.getGuildId()));
							guildMatchMap.put(swgd.getGuildId(), swmi);
							matchMap.put(swgd.getGuildId(), JSON.toJSONString(swmi));
						} else {
							//等待下一次的循环.
							tmpList.add(swgd);
							break;
						}
						
					}
				}
			}
		}
		
		if (!matchMap.isEmpty()) {
			RedisProxy.getInstance().addOrUpdateSimulateWarMatchInfo(termId, matchMap, keyExpireTime);
		}		
		RedisProxy.getInstance().setSimulateWarMatchFinish(termId, keyExpireTime);
		
		//先修改自己的acitivyt状态.
		this.guildMatchMap.putAll(guildMatchMap);
		this.activityInfo.finishState(SimulateWarActivityData.MATCH_FINISH);
		this.activityInfo.saveToRedis();
		
		logger.info("simulate war match over guildSize:{}", matchMap.size());
	}

	/**
	 * 同步玩家的三路出兵信息.
	 * @param player
	 */
	public void synPlayerAllMarchInfo(Player player) {
		
		String guildId = player.getGuildId();
		SimulateWarAllMarchResp.Builder sbuilder = SimulateWarAllMarchResp.newBuilder();
		if (!HawkOSOperator.isEmptyString(guildId)) {
			
			PBSimulateWarPlayer.Builder warPlayer = this.getPBSimulateWarPlayer(player.getId());			
			if (warPlayer != null && warPlayer.getPlayerInfo().getGuildID().equals(guildId)) {
				sbuilder.addAllBattleDatas(warPlayer.getBattleDatasList());
			}
		}					
		
		HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code.SIMULATE_WAR_ALL_MARCH_RESP_VALUE, sbuilder);
		player.sendProtocol(hawkProtocol);
	}
	
	@MessageHandler
	public void onSimulateWarDissolveMsg(SimulateWarDissolveMsg msg) {
		Player player = msg.getPlayer();
		int result = onSimulateWarDissolve(player, msg.getMarchId());
		if (result == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(HP.code.SIMULATE_WAR_DISSOLVE_REQ_VALUE);
		} else {
			player.sendError(HP.code.SIMULATE_WAR_DISSOLVE_REQ_VALUE, result, 0);
		}
	}
		
	private int onSimulateWarDissolve(Player player, String marchId) {
		PBSimulateWarPlayer.Builder warPlayer = this.getPBSimulateWarPlayer(player.getId());
		if (warPlayer == null) {
			return Status.Error.SIMULATE_WAR_NOT_FOUND_MARCH_VALUE;
		}
		String guildId = player.getGuildId();
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
		if (guild == null) {
			return Status.Error.GUILD_NO_JOIN_VALUE;
		}
		
		SimulateWarGuildData guildData = this.getGuildDat(guildId);
		if (guildData == null) {
			return Status.Error.SIMULATE_WAR_NOT_FOUND_GUILD_DATA_VALUE;
		}
		
		int index = -1;
		PBSimulateWarBattleData battleData = null;
		for (int i = 0; i < warPlayer.getBattleDatasList().size(); i ++) {
			 battleData = warPlayer.getBattleDatasList().get(i);
			if (battleData.getId().equals(marchId)) {
				index = i;
				break;
			}
		}
		
		if (index < 0 || battleData == null) {
			return Status.Error.SIMULATE_WAR_NOT_FOUND_MARCH_VALUE;
		}
		
		int termId = this.getTermId();
		int keyExpireTime = this.getKeyExpireTime();
		
		warPlayer.removeBattleDatas(index);
		RedisProxy.getInstance().addOrUpdateSimulateWarGuildPlayer(termId, guildId, warPlayer, keyExpireTime);
		this.subGuildTeamNum(termId, guildData, 1, battleData.getBattleValue());		
		removeGuildWayMarch(guildId, battleData.getWay(), battleData.getId());
		
		logger.info("dissolve march playerId:{}, marchId:{}", player.getId(), marchId);
		
		LogUtil.logSimulateWarSign(player, termId, guildId, battleData.getWay(), GsConst.SimulateWarConst.DISSOLVE);
		
		this.synPlayerAllMarchInfo(player);
		this.syanPageInfoUpdate(player);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	@MessageHandler
	private void onQuitGuild(SimulateWarQuitGuildMsg msg) {
		//非报名阶段不处理
		if (activityInfo.getState() != SimulateWarActivityState.SW_SIGN_UP) {
			return;
		}
		
		SimulateWarGuildData guildData = this.getGuildDat(msg.getGuildId());
		if (guildData == null) {			
			return;
		}
		
		PBSimulateWarPlayer.Builder warPlayer = this.removePBSimulateWarPlayer(msg.getPlayer().getId());
		if (warPlayer == null) {
			return;
		}
		
		long totalBattleValue = 0;
		Map<String, WayType> wayMap = new HashMap<>();
		for (PBSimulateWarBattleData battleData : warPlayer.getBattleDatasList()) {			
			totalBattleValue += battleData.getBattleValue();
			wayMap.put(battleData.getId(), battleData.getWay());
		}
		
		int termId = this.getTermId();				
		//减少工会的信息.
		this.subGuildTeamNum(termId, guildData, wayMap.size(), totalBattleValue);
						
		//更新单路的信息.
		for (Entry<String, WayType> entry : wayMap.entrySet()) {
			this.removeGuildWayMarch(msg.getGuildId(), entry.getValue(), entry.getKey());
			//删除单路行军的时候,记录一下tlog.
			LogUtil.logSimulateWarSign(msg.getPlayer(), this.getTermId(), msg.getGuildId(), entry.getValue(), 2);
		}
		
		RedisProxy.getInstance().removeSimulateWarGuildPlayer(termId, msg.getGuildId(), msg.getPlayer().getId());
				
		this.syncPageInfo(msg.getPlayer());
		
		//退出公会删除战斗数据.
		logger.info("quit guild delete simulate war battle data playerId:{}", msg.getPlayer().getId());
	}
	
	public int getTermId() {
		return this.activityInfo.getTermId();
	}
	
	@MessageHandler
	private void onSimulateWarAdjustWayMsg(SimulateWarAdjustWayMsg msg) {
		Player player = msg.getPlayer();
		int result = onSimulateWarAdjustWay(player, msg.getWayType(), msg.getMarchId());
		if (result == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(HP.code.SIMULATE_WAR_ADJUST_WAY_REQ_VALUE);
		} else {
			player.sendError(HP.code.SIMULATE_WAR_ADJUST_WAY_REQ_VALUE, result, 0);
		}
		
	}
		
	private int onSimulateWarAdjustWay(Player player, WayType wayType, String marchId) {			
		PBSimulateWarPlayer.Builder warPlayer = this.getPBSimulateWarPlayer(player.getId());
		String guildId = player.getGuildId();
		if (warPlayer == null) {
			logger.info("adjust way error guildId:{}, playerId:{}, wayType:{}, marchId:{}", guildId, player.getId(), wayType, marchId);
			
			return Status.Error.SIMULATE_WAR_NOT_FOUND_MARCH_VALUE;
		}		
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
		if (guild == null) {
			logger.info("adjust way error guildId:{}, playerId:{}, wayType:{}, marchId:{}", guildId, player.getId(), wayType, marchId);
			
			return Status.Error.GUILD_NO_JOIN_VALUE;
		}
		
		SimulateWarGuildData guildData = this.getGuildDat(guildId);
		if (guildData == null) {
			logger.info("adjust way error guildId:{}, playerId:{}, wayType:{}, marchId:{}", guildId, player.getId(), wayType, marchId);
			
			return Status.Error.SIMULATE_WAR_NOT_FOUND_GUILD_DATA_VALUE;
		}
		
		int index = -1;
		PBSimulateWarBattleData.Builder battleData = null;
		for (int i = 0; i < warPlayer.getBattleDatasBuilderList().size(); i ++) {
			 battleData = warPlayer.getBattleDatasBuilderList().get(i);
			if (battleData.getId().equals(marchId)) {
				index = i;
				break;
			}
		}
		
		if (index < 0 || battleData == null) {
			logger.info("adjust way error guildId:{}, playerId:{},  wayType:{}, marchId:{}", guildId, player.getId(), wayType, marchId);
			
			return Status.Error.SIMULATE_WAR_NOT_FOUND_MARCH_VALUE;
		}
		
		if (battleData.getWay() == wayType) {
			logger.info("adjust way error guildId:{}, playerId:{}, wayType:{}, marchId:{}", guildId, player.getId(), wayType, marchId);
			
			return Status.Error.SIMULATE_WAR_DUMPLICATE_WAY_VALUE;
		}
		WayType oldWay = battleData.getWay();
		battleData.setWay(wayType);
		this.removeGuildWayMarch(guildId, oldWay, marchId);
		this.addGuildWayMarch(guildId, wayType, marchId);
		
		RedisProxy.getInstance().addOrUpdateSimulateWarGuildPlayer(this.getTermId(), guildId, warPlayer, this.getKeyExpireTime());
		logger.info("adjust way guildId:{}, playerId:{}, oldWayType:{}, newWayType:{}, marchId:{}", guildId, player.getId(), oldWay, wayType, marchId);
		this.synPlayerAllMarchInfo(player);
		this.syanPageInfoUpdate(player);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	@MessageHandler
	private void onSimulateWarOrderAdjustMsg(SimulateWarOrderAdjustMsg msg) {
		Player player = msg.getPlayer();
		int result = onSimulatewarOrderAdjust(player, msg.getGuildId(), msg.getWayType(), msg.getMarchIdList());
		if (result == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(HP.code.SIMULATE_WAR_ORDER_ADJUST_REQ_VALUE);
		} else {
			player.sendError(HP.code.SIMULATE_WAR_ORDER_ADJUST_REQ_VALUE, result, 0);
		}
		
	}
	
	
	private int onSimulatewarOrderAdjust(Player player, String guildId, WayType wayType, List<String> strList) {
		logger.info("order adjust playerId:{}, guildId:{}, wayType:{}, strList:{}", player.getId(), guildId, wayType, strList);
		SimulateWarGuildData guildData = this.getGuildDat(guildId);
		if (guildData == null) {
			logger.info("adjust order error guildId:{}, playerId:{}, wayType:{}, marchId:{}", guildId, player.getId(), wayType);
			
			return Status.Error.SIMULATE_WAR_NOT_FOUND_GUILD_DATA_VALUE;
		}
		
		Map<WayType, List<String>> map = this.getGuildWayMarch(guildId);
		if (map == null) {
			logger.info("adjust order error guildId:{}, playerId:{}, wayType:{}, marchId:{}", guildId, player.getId(), wayType);
			
			return Status.Error.SIMULATE_WAR_NOT_FOUND_GUILD_DATA_VALUE;
		}
		
		List<String> oldMarchList = map.get(wayType);
		if (CollectionUtils.isEmpty(oldMarchList)) {
			logger.info("adjust order error guildId:{}, playerId:{}, wayType:{}, marchId:{}", guildId, player.getId(), wayType);
			
			return Status.Error.SIMULATE_WAR_NOT_FOUND_GUILD_DATA_VALUE;
		}
		
		if (oldMarchList.size() != strList.size() || !(oldMarchList.containsAll(strList))) {
			logger.info("adjust order error guildId:{}, playerId:{}, wayType:{}, marchId:{}", guildId, player.getId(), wayType);
			
			return Status.Error.SIMULATE_WAR_NOT_FOUND_GUILD_DATA_VALUE;
		}
		
		//拷贝一份吧，因为这个list是从pb过来的.
		map.put(wayType, new ArrayList<>(strList));
		RedisProxy.getInstance().addOrUpdateSimulateWarWayMarch(this.getTermId(), guildId, map, this.getKeyExpireTime());
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 同步单路的玩家信息.
	 * @param player
	 */
	public void synSimulateWarWayPlayerReq(Player player, String guildId, WayType wayType) {
		SimulateWarWayPlayersResp.Builder builder = SimulateWarWayPlayersResp.newBuilder();
		SimulateWarActivityState state = this.activityInfo.getState();
		label:
		{
			if (state != SimulateWarActivityState.SW_SIGN_UP && state != SimulateWarActivityState.SW_MANAGE) {
				break label;
			}
			Map<WayType, List<String>> map = this.getGuildWayMarch(guildId);
			if (map == null ) {
				break label;
			}
			List<String> marchIdList = map.get(wayType);
			if (CollectionUtils.isEmpty(marchIdList)) {
				break label;
			}
			//根据marchId取到玩家信息.
			String playerId = null;
			for (String marchId : marchIdList) {
				playerId = this.getPlayerIdByMarchId(marchId);
				PBSimulateWarPlayer.Builder warPlayer = this.getPBSimulateWarPlayer(playerId);
				if (warPlayer == null) {
					logger.error("in guildWayMarch but not in warPlayerMap playerId:{}, marchId:{}", playerId, marchId);
					
					continue;
				}
				PBSimulateWarWayPlayer.Builder wayPlayer = buildPBSimulateWarWayPlayer(warPlayer, marchId);
				if (wayPlayer.getBattleData() == null) {
					logger.error("marchId not found in warPlayer battle data playerId:{}, marchId:{}", playerId, marchId);
					
					continue;
				}
				
				builder.addPlayers(wayPlayer);
			}
		}
		builder.setWay(wayType);
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.SIMULATE_WAR_WAY_PLAYERS_RESP_VALUE, builder);
		player.sendProtocol(protocol);
	}
	
	private PBSimulateWarWayPlayer.Builder buildPBSimulateWarWayPlayer(PBSimulateWarPlayer.Builder warPlayer, String marchId) {
		PBSimulateWarWayPlayer.Builder wayPlayer = PBSimulateWarWayPlayer.newBuilder();
		wayPlayer.setPlayerInfo(warPlayer.getPlayerInfo());
		for (PBSimulateWarBattleData battleData : warPlayer.getBattleDatasList()) {
			if (battleData.getId().equals(marchId)) {
				wayPlayer.setBattleData(battleData);
			}
		}
		
		return wayPlayer;
	}	 
	
	private IWorldMarch buildMarch(SimulateWarBasePlayerStruct playerInfo,  PBSimulateWarBattleData.Builder sourceData) {
		 		
		TemporaryMarch atkMarch = new TemporaryMarch();
		List<ArmyInfo> armys = new ArrayList<>(sourceData.getSoldiersCount());
		for (PBSimulateWarSoldier sd : sourceData.getSoldiersList()) {
			if (sd.getCount() > 0) {
				armys.add(new ArmyInfo(sd.getArmyId(), sd.getCount()));
			}
		}
		
		SimulateWarGuildData guildData = this.getEnemyGuildData(playerInfo.getGuildID());
		Map<EffType, Integer> extMap = new HashMap<>();
		if (guildData != null) {
			SimulateWarEncourageCfg encourageCfg = HawkConfigManager.getInstance().getConfigByKey(SimulateWarEncourageCfg.class, guildData.getEncourageTimes());
			if (encourageCfg != null) {
				extMap.putAll(encourageCfg.getBuffMap());
			}
		}
		SimulateWarPlayer player = new SimulateWarPlayer(HawkXID.nullXid(), playerInfo, sourceData.build(), extMap);
		atkMarch.setArmys(armys);
		atkMarch.setPlayer(player);
		atkMarch.getMarchEntity().setArmourSuit(ArmourSuitType.ONE_VALUE);
		atkMarch.getMarchEntity().setMechacoreSuit(MechaCoreSuitType.MECHA_ONE_VALUE);
		atkMarch.getMarchEntity().setHeroIdList(sourceData.getHerosList().stream().map(PBHeroInfo::getHeroId).collect(Collectors.toList()));
		atkMarch.getMarchEntity().setSuperSoldierId(sourceData.getSuperSoldier().getSuperSoldierId());
		atkMarch.setHeros(player.getHeroByCfgId(null));
		atkMarch.getMarchEntity().setDressList(sourceData.getDressIdList());;
		return atkMarch;
	}
	
	//轮空的玩家.
	private void doGuildBattle(int termId, SimulateWarGuildData guildDataA) {
		logger.info("guildId:{} no match guildId", guildDataA.getGuildId());
		//统计数据.
		int keyExpireTime = this.getKeyExpireTime();
		SimulateWarGuildBattle.Builder guildBattle = SimulateWarGuildBattle.newBuilder();
		Map<WayType, List<String>> wayMap = this.getGuildWayMarch(guildDataA.getGuildId());
		PBSimulateWarPlayer.Builder mvp = null;
		List<WayNumInfo> guildAWayLeftNumList = new ArrayList<>();
		for (Entry<WayType, List<String>> wayMarchs : wayMap.entrySet()) {
			WayNumInfo.Builder wayNumInfo = WayNumInfo.newBuilder();
			wayNumInfo.setWay(wayMarchs.getKey());
			wayNumInfo.setNum(wayMarchs.getValue().size());
			guildAWayLeftNumList.add(wayNumInfo.build());
			for (String marchId : wayMarchs.getValue()) {
				String marchPlayerId =this.getPlayerIdByMarchId(marchId);
				PBSimulateWarPlayer.Builder aPlayer = this.getPBSimulateWarPlayer(marchPlayerId);
				if (mvp == null) {
					mvp = aPlayer;
				} else if (aPlayer.getPlayerInfo().getBattlePoint() > mvp.getPlayerInfo().getBattlePoint()) {
					mvp = aPlayer;
				}
			}
		}
		
		SimulateWarBattlePlayer.Builder mvpBattlePlayer = SimulateWarBattlePlayer.newBuilder();
		mvpBattlePlayer.setArmyCnt(0);
		mvpBattlePlayer.setDisCnt(0);
		mvpBattlePlayer.setKillCount(0);
		mvpBattlePlayer.setPlayer(mvp.getPlayerInfo());
		guildBattle.setGuildA(this.buildSimulateWarGuildinfo(guildDataA, 3, guildAWayLeftNumList, guildAWayLeftNumList, mvpBattlePlayer.build()));
		guildBattle.setWinGuildId(guildDataA.getGuildId());
		
		RedisProxy.getInstance().addOrUpdateSimulateWarGuildBattle(termId, guildDataA.getGuildId(), guildBattle.build(), keyExpireTime);
	}
	/**
	 * 在战斗之前还有一个阶段就是加载数据.
	 * @param termId
	 * @param matchInfo
	 */
	private void doGuildBattle(int termId, SimulateWarGuildData guildDataA, SimulateWarGuildData guildDataB) {
		logger.info("guildA:{} vs guildB:{}", guildDataA.getGuildId(), guildDataB.getGuildId());
		int keyExpireTime = this.getKeyExpireTime();
		String guildA = guildDataA.getGuildId();
		String guildB = guildDataB.getGuildId();		
		Map<WayType, List<String>> guildAMarchListMap = this.getGuildWayMarch(guildA);
		Map<WayType, List<String>> guildBMarchListMap = this.getGuildWayMarch(guildB);
		Map<String, SimulateWarBattlePlayer.Builder> guildABattlePlayerMap = new HashMap<>();
		Map<String, SimulateWarBattlePlayer.Builder> guildBBattlePlayerMap = new HashMap<>();
		Map<String, Integer> playerWayKillCountMap = new HashMap<>();
		
		int guildAWinCount = 0;
		int guildBWinCount = 0;
		String guildBattleId = HawkUUIDGenerator.genUUID();
		//主区先打吧.
		SimulateWarGuildBattle.Builder guildBattle = SimulateWarGuildBattle.newBuilder();
		guildBattle.setGBattleId(guildBattleId);
		List<WayNumInfo> guildAWayLeftNumList = new ArrayList<>();
		List<WayNumInfo> guildBWayLeftNumList = new ArrayList<>();
		List<WayNumInfo> guildATotalNumList = new ArrayList<>();
		List<WayNumInfo> guildBTotalNumList = new ArrayList<>();
		for (Entry<WayType, List<String>> guildAEntry : guildAMarchListMap.entrySet()) {
			WayNumInfo.Builder wayNumInfo = WayNumInfo.newBuilder();
			wayNumInfo.setWay(guildAEntry.getKey());
			wayNumInfo.setNum(guildAEntry.getValue().size());
			guildATotalNumList.add(wayNumInfo.build());
		}
		
		for (Entry<WayType, List<String>> guildBEntry : guildBMarchListMap.entrySet()) {
			WayNumInfo.Builder wayNumInfo = WayNumInfo.newBuilder();
			wayNumInfo.setWay(guildBEntry.getKey());
			wayNumInfo.setNum(guildBEntry.getValue().size());
			guildBTotalNumList.add(wayNumInfo.build());
		}
		Set<WayType> emptyWay = new HashSet<>();
		for(Entry<WayType, List<String>> guildAEntry : guildAMarchListMap.entrySet()) {
			WayType way = guildAEntry.getKey();
			List<String> guildAMarchList = guildAEntry.getValue();
			List<String> guildBMarchList = guildBMarchListMap.get(way);
			//空的.
			if (CollectionUtils.isEmpty(guildAMarchList) && CollectionUtils.isEmpty(guildBMarchList)) {
				emptyWay.add(way);
				
				continue;
			}
			//怂了
			if (guildAMarchList == null) {
				guildAMarchList = new ArrayList<>();
			}
			
			if (guildBMarchList == null) {
				guildBMarchList = new ArrayList<>();
			}
			
			
			int aIndex = 0;
			int bIndex = 0;
			SimulateWarBattleList.Builder battleList = SimulateWarBattleList.newBuilder(); 
			String winGuild = guildDataA.getTeamNum() > guildDataB.getTeamNum() ? guildA : guildB;
			WayNumInfo.Builder guildAWayNum = WayNumInfo.newBuilder(); 
			guildAWayNum.setWay(way);
			WayNumInfo.Builder guildBWayNum = WayNumInfo.newBuilder();
			guildBWayNum.setWay(way);
			for (int a = 0; a < guildAMarchList.size(); a++) {				
				for (int b = bIndex; b < guildBMarchList.size(); b++) {
					bIndex = b;
					String aMarchId = guildAMarchList.get(aIndex);
					String bMarchId = guildBMarchList.get(bIndex);
					SimulateWarBattelInfo.Builder battleInfo = SimulateWarBattelInfo.newBuilder(); 
					String battleId = HawkOSOperator.randomUUID();
					battleInfo.setPBattleId(battleId);
					
					// todo 这里在做个判空的兼容,外面也需要兼容.				
					String aPlayerId = this.getPlayerIdByMarchId(aMarchId);
					PBSimulateWarPlayer.Builder aPlayer = this.getPBSimulateWarPlayer(aPlayerId);
					PBSimulateWarBattleData.Builder aBattleData = this.getPBSimulateWarBattleData(aPlayer, aMarchId);
					String bPlayerId = this.getPlayerIdByMarchId(bMarchId);
					PBSimulateWarPlayer.Builder bPlayer = this.getPBSimulateWarPlayer(bPlayerId);
					PBSimulateWarBattleData.Builder bBattleData = this.getPBSimulateWarBattleData(bPlayer, bMarchId);
					
					int armyA = calcArmyCnt(aBattleData);
					int armyB = calcArmyCnt(bBattleData);
					if (armyA <= 0) {
						aIndex ++;
						break;
					}
					if (armyB <= 0) {
						bIndex ++;
						continue;
					}
					BattleOutcome battleOutcome = doFight(termId, battleId, aPlayer.getPlayerInfo(), aBattleData, bPlayer.getPlayerInfo(), bBattleData, way);
					boolean isAtkWin = battleOutcome.isAtkWin();					
					int armyAftA = calcArmyCnt(aBattleData);
					int armyAftB = calcArmyCnt(bBattleData);
					logger.info("battleId:{} aPlayerId:{} bPlayerId:{} wayType:{} isAtkWin:{}, aMarchId:{}, bMarchId:{}, armyA:{},armyAftA:{},armyB:{},armyAftB:{}", 
							battleId, aPlayerId, bPlayerId, way, isAtkWin, aMarchId, bMarchId, armyA, armyAftA, armyB, armyAftB);
					SimulateWarBattlePlayer.Builder battleAPlayer = guildABattlePlayerMap.get(aPlayerId);
					if (battleAPlayer == null) {
						battleAPlayer = SimulateWarBattlePlayer.newBuilder();
						battleAPlayer.setPlayer(aPlayer.getPlayerInfo());
						guildABattlePlayerMap.put(aPlayerId, battleAPlayer);						
					}					
					battleAPlayer.setArmyCnt(armyA);
					battleAPlayer.setDisCnt(armyA - armyAftA);
					battleAPlayer.setMarchId(aMarchId);
					
					SimulateWarBattlePlayer.Builder battleBPlayer = guildBBattlePlayerMap.get(bPlayerId);
					if (battleBPlayer == null) {
						battleBPlayer = SimulateWarBattlePlayer.newBuilder();
						battleBPlayer.setPlayer(bPlayer.getPlayerInfo());
						guildBBattlePlayerMap.put(bPlayerId, battleBPlayer);
					}
					battleBPlayer.setArmyCnt(armyB);
					battleBPlayer.setDisCnt(armyB- armyAftB);
					battleBPlayer.setMarchId(bMarchId);
										
					if (isAtkWin) {						
						winGuild = guildA;
						MapUtil.appendIntValue(playerWayKillCountMap, aMarchId, 1);
						int marchKillCount = playerWayKillCountMap.getOrDefault(aMarchId, 0);						
						int totalKillCount = battleAPlayer.getKillCount() + 1;
						battleAPlayer.setKillCount(marchKillCount);
						battleInfo.setWinnerId(aPlayerId);
						battleInfo.setPlayerA(battleAPlayer);
						battleInfo.setPlayerB(battleBPlayer);
						battleList.addBattleInfos(battleInfo.build());						
						bIndex ++;
						//计算总共的杀敌数量.
						battleAPlayer.setKillCount(totalKillCount);
					} else {
						winGuild = guildB;
						MapUtil.appendIntValue(playerWayKillCountMap, bMarchId, 1);
						int marchKillCount = playerWayKillCountMap.getOrDefault(bMarchId, 0);						
						int totalKillCount = battleBPlayer.getKillCount() + 1;
						battleBPlayer.setKillCount(marchKillCount);																	
						battleInfo.setWinnerId(bPlayerId);	
						battleInfo.setPlayerA(battleAPlayer);
						battleInfo.setPlayerB(battleBPlayer);
						battleList.addBattleInfos(battleInfo.build());
						aIndex ++;
						//总共的杀敌数量
						battleBPlayer.setKillCount(totalKillCount);
						break;
					}									
				}								
			}
			
			if (aIndex < guildAMarchList.size() - 1 || (aIndex == 0 && guildAMarchList.size() == 1)) {				
				addEmptyBattle(guildAMarchList, battleList, aIndex + 1, true);
				winGuild = guildA;
				guildAWayNum.setNum(guildAMarchList.size() - (aIndex  + 1) + 1);
				guildBWayNum.setNum(0);
			} else if (bIndex < guildBMarchList.size() - 1 || (bIndex == 0 && guildBMarchList.size() == 1)) {				
				addEmptyBattle(guildBMarchList, battleList, bIndex + 1, false);
				winGuild = guildB;
				guildBWayNum.setNum(guildBMarchList.size() - (bIndex  + 1) + 1);
				guildAWayNum.setNum(0);
			} else {
				guildAWayNum.setNum(0);
				guildBWayNum.setNum(0);
				if (winGuild.equals(guildB) && !guildBMarchList.isEmpty()) {
					guildBWayNum.setNum(1);
				} else {
					if (!guildAMarchList.isEmpty()) {
						guildAWayNum.setNum(1);
					}					
				}				 
				
			}
			
			guildAWayLeftNumList.add(guildAWayNum.build());
			guildBWayLeftNumList.add(guildBWayNum.build());
			
			if (winGuild.equals(guildA)) {
				guildAWinCount++;
			} else {
				guildBWinCount++;
			}
			
			RedisProxy.getInstance().addOrUpdateSimulateWarBattleRecord(termId, guildBattleId, way, battleList.build(), keyExpireTime);
		}
		
		//不可能三路为空
		if (!emptyWay.isEmpty()) {
			if (emptyWay.size() == 2) {
				//空两路
				if (guildAWinCount > guildBWinCount) {
					guildAWinCount += 2;
				} else {
					guildBWinCount += 2;
				}
			} else {
				//空一路.
				//两人都是赢一路
				if (guildAWinCount == guildBWinCount) {
					if (guildDataA.getTeamNum() >= guildDataB.getTeamNum()) {
						guildAWinCount += 1;
					} else {
						guildBWinCount += 1;
					}
				} else {
					if (guildAWinCount > guildBWinCount) {
						guildAWinCount += 1;
					} else {
						guildBWinCount += 1;
					}
				}
				
			}
		}		
		
		List<SimulateWarBattlePlayer.Builder> guildAPlayerList = new ArrayList<>(guildABattlePlayerMap.values());
		List<SimulateWarBattlePlayer.Builder> guildBPlayerList = new ArrayList<>(guildBBattlePlayerMap.values());
		Collections.sort(guildAPlayerList, new SimulateWarMvpCompartor());
		Collections.sort(guildBPlayerList, new SimulateWarMvpCompartor());
		guildBattle.setGuildA(this.buildSimulateWarGuildinfo(guildDataA, guildAWinCount, guildATotalNumList, guildAWayLeftNumList, guildAPlayerList.get(0).build()));
		guildBattle.setGuildB(this.buildSimulateWarGuildinfo(guildDataB, guildBWinCount, guildBTotalNumList, guildBWayLeftNumList, guildBPlayerList.get(0).build()));
		guildBattle.setWinGuildId(guildAWinCount > guildBWinCount ? guildA : guildB);
		
		SimulateWarGuildBattle swgb = guildBattle.build();
		//更新战斗记录.
		RedisProxy.getInstance().addOrUpdateSimulateWarGuildBattle(termId, guildA, swgb, keyExpireTime);
		RedisProxy.getInstance().addOrUpdateSimulateWarGuildBattle(termId, guildB, swgb, keyExpireTime);
	}
	
	private void addEmptyBattle(List<String> marchIdList, SimulateWarBattleList.Builder list, int aIndex, boolean isAtker) {
		for (int i = aIndex; i < marchIdList.size(); i++) {
			String marchId = marchIdList.get(i);
			SimulateWarBattelInfo.Builder battleInfo = SimulateWarBattelInfo.newBuilder(); 
			String battleId = HawkOSOperator.randomUUID();
			battleInfo.setPBattleId(battleId);			
			String aPlayerId = this.getPlayerIdByMarchId(marchId);
			PBSimulateWarPlayer.Builder warPlayer = this.getPBSimulateWarPlayer(aPlayerId);
			SimulateWarBattlePlayer.Builder battlePlayer = SimulateWarBattlePlayer.newBuilder();
			battlePlayer.setPlayer(warPlayer.getPlayerInfo());			
			PBSimulateWarBattleData.Builder bBattleData = this.getPBSimulateWarBattleData(warPlayer, marchId);			
			int armyA = calcArmyCnt(bBattleData);
			battlePlayer.setArmyCnt(armyA);
			battlePlayer.setDisCnt(0);
			battlePlayer.setMarchId(bBattleData.getId());
			battleInfo.setWinnerId(warPlayer.getPlayerInfo().getPlayerId());
			if (isAtker) {
				battleInfo.setPlayerA(battlePlayer);
			} else {
				battleInfo.setPlayerB(battlePlayer);
			}
			list.addBattleInfos(battleInfo.build());
		}
	}
	
	public BattleOutcome doFight(int termId, String battleId, SimulateWarBasePlayerStruct atkBasePlayer,
			PBSimulateWarBattleData.Builder atkBattleData, SimulateWarBasePlayerStruct defBasePlayer,
			PBSimulateWarBattleData.Builder defBattleData, WayType way) {
		IWorldMarch atkMarch = buildMarch(atkBasePlayer, atkBattleData);

		IWorldMarch defMarch = buildMarch(defBasePlayer, defBattleData);

		List<Player> atkPlayers = new ArrayList<>();
		atkPlayers.add(atkMarch.getPlayer());

		// 防守方玩家
		List<Player> defPlayers = new ArrayList<>();
		defPlayers.add(defMarch.getPlayer());

		// 进攻方行军
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		atkMarchs.add(atkMarch);
		// 防守方行军
		List<IWorldMarch> defMarchs = new ArrayList<>();
		defMarchs.add(defMarch);

		// 战斗数据输入
		PvpBattleIncome battleIncome = BattleService.getInstance().initPVPBattleData(BattleConst.BattleType.SIMULATE_WAR, 0, atkPlayers, defPlayers, atkMarchs, defMarchs,
				BattleSkillType.BATTLE_SKILL_NONE);
		battleIncome.getBattle().setDuntype(DungeonMailType.TBLY);
		// 战斗数据输出
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		FightMailService.getInstance().recordSimulateWarMail(termId, battleId, battleIncome, battleOutcome, atkBasePlayer, defBasePlayer);
		updateArmyData(atkBattleData, battleOutcome.getAftArmyMapAtk().get(atkMarch.getPlayerId()));
		updateArmyData(defBattleData, battleOutcome.getAftArmyMapDef().get(defMarch.getPlayerId()));
		if (battleIncome.getBattle().isSaveDebugLog()) {
			String filename = HawkAppCfg.getInstance().getLogPath() + File.separator + way + "-" + atkBasePlayer.getName() + "VS" + defBasePlayer.getName() + "==" + battleId;
			HawkOSOperator.saveAsFile(battleIncome.getBattle().getDebugLog(), filename);
		}
		return battleOutcome;
	}
	
	/**
	 * 
	 * @param warPlayer
	 * @param marchId
	 * @return
	 */
	private PBSimulateWarBattleData.Builder getPBSimulateWarBattleData(PBSimulateWarPlayer.Builder warPlayer, String marchId) {
		List<PBSimulateWarBattleData.Builder> battleDataBuilderList = warPlayer.getBattleDatasBuilderList();
		for (PBSimulateWarBattleData.Builder battleDataBuilder : battleDataBuilderList) {
			if (battleDataBuilder.getId().equals(marchId)) {
				return battleDataBuilder;
			}
		}
		
		return null;
	}
	
	private void updateArmyData(PBSimulateWarBattleData.Builder atkData, List<ArmyInfo> armyList) {
		List<PBSimulateWarSoldier.Builder> soldierList = atkData.getSoldiersBuilderList();
		for(PBSimulateWarSoldier.Builder builder : soldierList){
			int armyId = builder.getArmyId();
			for(ArmyInfo army : armyList){
				if(army.getArmyId() == armyId){
					builder.setCount(army.getFreeCnt());
				}
			}
		}
	}
	
	private int calcArmyCnt(PBSimulateWarBattleData.Builder simulateWarBattleData) {
		int sum = 0;
		List<PBSimulateWarSoldier> list = simulateWarBattleData.getSoldiersList();
		if (list == null || list.isEmpty()) {
			return sum;
		}
		for (PBSimulateWarSoldier soldier : list) {
			sum += soldier.getCount();
		}
		return sum;
	}
	
	/**
	 * 构建公会信息.
	 * @param guildData
	 * @param count
	 * @param wayNumInfo
	 * @param mvpPlayer
	 * @return
	 */
	private SimulateWarGuildInfo.Builder buildSimulateWarGuildinfo(SimulateWarGuildData guildData, int count, 
			List<WayNumInfo> totalNumInfo, List<WayNumInfo> wayNumInfo, SimulateWarBattlePlayer mvpPlayer) {
		SimulateWarGuildInfo.Builder dataBuilder = SimulateWarGuildInfo.newBuilder();
		dataBuilder.setId(guildData.getGuildId());
		dataBuilder.setName(guildData.getGuildName());
		dataBuilder.setTag(guildData.getGuildTag());
		dataBuilder.setTeamCount(guildData.getTeamNum());
		dataBuilder.setTotalPower(guildData.getBattleValue());
		dataBuilder.setWinCount(count);
		dataBuilder.addAllLeftNumInfo(wayNumInfo);
		dataBuilder.addAllTotalNumInfo(totalNumInfo);
		dataBuilder.setMvpPlayer(mvpPlayer); 
		dataBuilder.setServerId(GlobalData.getInstance().getMainServerId(mvpPlayer.getPlayer().getServerId()));
		dataBuilder.setGuildTag(guildData.getGuildFlag());
		
		return dataBuilder;
	}
	
	/**
	 * 同步战斗记录.
	 * @param player
	 * @param guildId
	 * @param way
	 */
	public void synBattleRecord(Player player, String guildId, WayType way) {
		SimulateWarBattleList battleList = getOrLoadWayBattleList(guildId, way);
		SimulateWarBattleRecordResp.Builder sbuilder = SimulateWarBattleRecordResp.newBuilder();
		sbuilder.setWay(way);
		if (battleList != null) {
			sbuilder.addAllBattleInfo(battleList.getBattleInfosList());															
		}
		
		HawkProtocol synProtocol = HawkProtocol.valueOf(HP.code.SIMULATE_WAR_BATTLE_RECORD_RESP_VALUE, sbuilder);
		player.sendProtocol(synProtocol);
	}
	
	/**
	 * 拉取
	 * @param guildId
	 * @return
	 */
	private SimulateWarBattleList getOrLoadWayBattleList(String guildId, WayType way) {
		Map<WayType, SimulateWarBattleList> map = this.getOrLoadGuildBattleList(guildId);
		if (MapUtils.isEmpty(map)) {
			return null;
		} else {
			return map.get(way);
		}
	}
	
	private Map<WayType, SimulateWarBattleList> getOrLoadGuildBattleList(String guildId) {
		Map<WayType, SimulateWarBattleList> listMap = this.guildBattleRecordMap.get(guildId);
		if (listMap != null) {
			return listMap;
		} else {
			SimulateWarGuildBattle.Builder battleBuilder = RedisProxy.getInstance().getSimulateWarGuildBattle(activityInfo.getTermId(), guildId);
			if (battleBuilder != null) {
				Map<WayType, SimulateWarBattleList> map = RedisProxy.getInstance().getSimulateWarBattleRecord(this.getTermId(), battleBuilder.getGBattleId());
				this.guildBattleRecordMap.put(guildId, map);
				
				return map;
			} else {
				return new HashMap<>();
			}							
		}
	}
	
	/**
	 * 同步主界面更新.
	 * @param player
	 */
	private void syanPageInfoUpdate(Player player) {
		SimulateWarGuildData guildData = this.getGuildDat(player.getGuildId());
		Map<WayType, List<String>> wayMap = this.getGuildWayMarch(player.getGuildId());
		if (guildData != null) {
			SimulateWarPageInfoUpdate.Builder sbuilder = SimulateWarPageInfoUpdate.newBuilder();
			sbuilder.setTeamTotalCount(guildData.getTeamNum());
			for (Entry<WayType, List<String>> entry : wayMap.entrySet()) {
				WayNumInfo.Builder wayNumInfo = WayNumInfo.newBuilder();
				wayNumInfo.setWay(entry.getKey());
				wayNumInfo.setNum(entry.getValue().size());
				sbuilder.addNumInfo(wayNumInfo);
			}
			
			HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code.SIMULATE_WAR_PAGE_INFO_UPDATE_VALUE, sbuilder);
			player.sendProtocol(hawkProtocol);
		}
				
	}
	
	/**
	 * 是否可以解除工会。
	 * @param guidlId
	 * @return
	 */
	public boolean checkDissolveGuild(String guildId) {
		SimulateWarGuildData guildData = this.getGuildDat(guildId);
		if (guildData == null) {
			return true;
		}
		
		//未开启阶段解散都是可以的.
		SimulateWarActivityState state = this.activityInfo.getState();
		if (state == SimulateWarActivityState.SW_HIDDEN || state == SimulateWarActivityState.SW_NOT_OPEN) {
			return true; 
		}
		
		//报名阶段只要有报名信息就不让解散.
		if (state == SimulateWarActivityState.SW_SIGN_UP) {
			return false;
		}
		
		//等把报名信息刷进去.
		if (!this.activityInfo.hasFinishState(SimulateWarActivityData.FLUSH_SIGN)) {
			return false;
		}
		
		//这里取报名数据, 因为担心match 信息有延迟.
		SimulateWarConstCfg constCfg = SimulateWarConstCfg.getInstance();
		if (guildData.getTeamNum() < constCfg.getMinTeamNum()) {
			return true;
		}
		
		
		return false;
	}
}
  

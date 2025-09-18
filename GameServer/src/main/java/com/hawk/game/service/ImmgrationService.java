package com.hawk.game.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.hawk.collection.ConcurrentHashSet;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;
import org.hawk.tuple.HawkTuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.impl.backImmigration.BackImmgrationActivity;
import com.hawk.activity.type.impl.immgration.cfg.ImmgrationActivityKVCfg;
import com.hawk.activity.type.impl.immgration.cfg.ImmgrationItemUseCfg;
import com.hawk.common.AccountRoleInfo;
import com.hawk.common.ServerInfo;
import com.hawk.game.GsConfig;
import com.hawk.game.activity.impl.yurirevenge.YuriRevengeService;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.crossproxy.ProxyHeader;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.PlayerBaseEntity;
import com.hawk.game.entity.PlayerEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisKey;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.invoker.BanPersonalRankMsgInvoker;
import com.hawk.game.player.Player;
import com.hawk.game.player.cache.PlayerDataKey;
import com.hawk.game.player.cache.PlayerDataSerializer;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Immgration.ImmgrationServerReq;
import com.hawk.game.protocol.Immgration.ImmgrationServerResp;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Rank.RankInfo;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.Status;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.gamelib.rank.RankScoreHelper;
import com.hawk.log.Action;

/**
 * 迁服服务类
 * @author Golden
 *
 */
public class ImmgrationService {
	
	static Logger logger = LoggerFactory.getLogger("Server");
	
	/**
	 * 正在迁服进行中的玩家
	 */
	private Set<String> immgratingPlayers = new ConcurrentHashSet<>();

	private static ImmgrationService instance = null;

	public static ImmgrationService getInstance() {
		if (instance == null) {
			instance = new ImmgrationService();
		}
		return instance;
	}
	
	/**
	 * 初始化
	 * @return
	 */
	public boolean init() {
		return true;
	}
	
	/**
	 * 迁服处理(A迁B，B处理)
	 * @param protocol
	 */
	public void onImmgrationReq(HawkProtocol protocol) {
		ProxyHeader proxyHeader = (ProxyHeader)protocol.getUserData();
		String playerId = proxyHeader.getSource();
		String fromServer = proxyHeader.getFrom();
		// 迁服请求信息
		ImmgrationServerReq reqInfo = protocol.parseProtocol(ImmgrationServerReq.getDefaultInstance());
		logger.info("player immgration, onImmgrationReq, begin, playerId: {}, fromServer: {}", playerId, fromServer);

		//反序列化数据
		HawkTuple3<String, PlayerBaseEntity, BuildingBaseEntity> tuple = unserializeImmigrationData(playerId, reqInfo);
		logger.info("player immgration, onImmgrationReq, end, playerId: {}, fromServer: {}", playerId, fromServer);
		String playerName = tuple.first;
		PlayerBaseEntity playerBase = tuple.second;
		BuildingBaseEntity building = tuple.third;
		
		// 回包
		ImmgrationServerResp.Builder builder = ImmgrationServerResp.newBuilder();
		builder.setCode(0);
		builder.setTarServerId(reqInfo.getTarServerId());
		HawkProtocol sendProtocol = HawkProtocol.valueOf(HP.code2.IMMGRATION_SERVER_RESP, builder);
		CrossProxy.getInstance().sendNotify(sendProtocol, proxyHeader.getFrom(), playerId, null);
		
		String serverId = GsConfig.getInstance().getServerId();
		// 添加迁服人数
		RedisProxy.getInstance().immgratioinNumAdd(getImmgrationActivityTermId(), serverId);
		// 添加进迁入玩家列表
		GlobalData.getInstance().addImmgrationInPlayerIds(playerId);
		// 设置迁服完成
		RedisProxy.getInstance().setPlayerImmgration(getImmgrationActivityTermId(), playerId);
		// 清除玩家所有邮件
		MailService.getInstance().clearAll(playerId);
		// 刷新排行榜信息
		refreshRankInfo(playerId, building, playerBase);
		
		// 发迁服成功邮件
		ServerInfo serverInfo = RedisProxy.getInstance().getServerInfo(serverId);
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(playerId)
				.setMailId(MailId.IMMGRATION_SUCCESS)
				.addContents(serverInfo.getName())
				.addSubTitles(serverInfo.getName())
				.build());
		//发跑马灯
		this.addBackImmgrationNotice(playerId, playerName);
		logger.info("player immgration, onImmgrationReq, finish, playerId: {}, fromServer: {}", playerId, fromServer);
	}
	
	/**
	 * 反序列化数据
	 */
	@SuppressWarnings("unchecked")
	private HawkTuple3<String, PlayerBaseEntity, BuildingBaseEntity> unserializeImmigrationData(String playerId, ImmgrationServerReq reqInfo) {
		BuildingBaseEntity building = null;
		PlayerBaseEntity playerBase = null;
		String playerName = "";
		for (PlayerDataKey dataKey : EnumSet.allOf(PlayerDataKey.class)) {
			try {
				// recharge表不处理（注意：以下几行代码只在trunk被注释掉了，后面分支都是正常存在的）
				// recharge表不处理
				if (dataKey == PlayerDataKey.PlayerRechargeEntities) {
					continue;
				}
				
				// 方尖碑不处理
				if (dataKey == PlayerDataKey.ObeliskEntities) { 
					continue;
				}
				
				// 从redis读取数据
				byte[] bytes = RedisProxy.getInstance().getRedisSession().hGetBytes("player_data:" + playerId, dataKey.name());
					
				// 反序列化
				Object data = PlayerDataSerializer.unserializeData(dataKey, bytes, false);
				if (data == null) {
					continue;
				}
				
				if (!dataKey.listMode()) {
					HawkDBEntity entity = (HawkDBEntity) data;
					if (dataKey == PlayerDataKey.PlayerEntity) {
						PlayerEntity playerEntity = (PlayerEntity) data;
						playerEntity.setServerId(reqInfo.getTarServerId());
						immgratingPlayers.add(playerId);
						GlobalData.getInstance().updateAccountInfo(playerEntity.getPuid(), reqInfo.getTarServerId(), playerId, 0, playerEntity.getName());
						playerName = playerEntity.getName();
					}
					
					if (dataKey == PlayerDataKey.PlayerBaseEntity) {
						PlayerBaseEntity playerBaseEntity = (PlayerBaseEntity) data;
						playerBaseEntity.setSaveAmt(0);
						playerBaseEntity._setChargeAmt(0);
						playerBase = playerBaseEntity;
					}
					
					entity.setPersistable(true);
					entity.create();
				} else {
					List<HawkDBEntity> entityList = (List<HawkDBEntity>) data;
					for (HawkDBEntity entity : entityList) {
						try {
							entity.setPersistable(true);
							entity.create();
							if (dataKey == PlayerDataKey.BuildingEntities) {
								BuildingBaseEntity buildingEntity = (BuildingBaseEntity) entity;
								if (buildingEntity.getType() == BuildingType.CONSTRUCTION_FACTORY_VALUE) {
									building = buildingEntity;
								}
							}
						} catch (Exception e) {
							HawkException.catchException(e);
						}
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);				
			}
		}
		
		// 反序列化活动数据
		immgrationActivityData(playerId);
		immgratingPlayers.remove(playerId);
		
		return new HawkTuple3<String, PlayerBaseEntity, BuildingBaseEntity>(playerName, playerBase, building);
	}
	
	/**
	 * 判断玩家是否正在迁服进行中
	 * @param playerId
	 * @return
	 */
	public boolean isPlayerImmigrating(String playerId) {
		return immgratingPlayers.contains(playerId);
	}
	
	/**
	 * 刷新排行榜信息
	 * @param playerId
	 * @param building
	 * @param playerBase
	 */
	private void refreshRankInfo(String playerId, BuildingBaseEntity building, PlayerBaseEntity playerBase) {
		//刷新大本等级排行榜
		if (building != null) {
			try {
				BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, building.getBuildingCfgId());
				int rankScore = buildingCfg.getLevel(), honor = buildingCfg.getHonor(), progress = buildingCfg.getProgress();
				if (honor > 0 || progress > 0) {
					rankScore = buildingCfg.getLevel() * RankService.HONOR_CITY_OFFSET + progress;
				}
				long upgradeTime = building.getLastUpgradeTime();
				if (buildingCfg.getLevel() >= 40) {
					upgradeTime = GlobalData.getInstance().getCityRankTime(playerId);
				}

				long value = upgradeTime/1000 - RankScoreHelper.rankSpecialSeconds;
				long score = Long.valueOf(rankScore + "" + (RankScoreHelper.rankSpecialOffset - value));
				LocalRedis.getInstance().updateRankScore(RankType.PLAYER_CASTLE_KEY, score, playerId);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		//刷新指挥官等级排行榜
		if (playerBase != null) {
			try {
				long calcTime = playerBase.getLevelUpTime();
				if (calcTime <= 0) {
					String timeStr = RedisProxy.getInstance().getRedisSession().hGet(RedisKey.PLAYER_LEVELUP_TIME, playerBase.getPlayerId());
					if (!HawkOSOperator.isEmptyString(timeStr)) {
						calcTime = Long.parseLong(timeStr);
						playerBase.setLevelUpTime(calcTime);
					}
				}
				if (calcTime <= 0) {
					calcTime = HawkTime.getSeconds();
				} else {
					calcTime = calcTime/1000;
				}
				
				long value = calcTime - RankScoreHelper.rankSpecialSeconds;
				long calcScore = Long.valueOf(playerBase.getLevel() + "" + (RankScoreHelper.rankSpecialOffset - value));
				LocalRedis.getInstance().updateRankScore(RankType.PLAYER_GRADE_KEY, calcScore, playerId);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 反序列化活动数据
	 * @param playerId
	 */
	private void immgrationActivityData(String playerId) {
		String key = "player_data_activity:" + playerId;
		HawkRedisSession session = RedisProxy.getInstance().getRedisSession();
		Map<byte[], byte[]> infos = session.hGetAllBytes(key.getBytes());
		if (infos == null || infos.isEmpty()) {
			return;
		}
		for (Entry<byte[], byte[]> info : infos.entrySet()) {
			try {
				// 活动ID
				String activityId = new String(info.getKey());
				// 数据
				byte[] data = info.getValue();
				
				// 反序列化存储
				com.hawk.activity.type.ActivityType type = com.hawk.activity.type.ActivityType.getType(Integer.parseInt(activityId));
				Class<? extends HawkDBEntity> clz = type.getDbEntity();
				if (clz == null) {
					logger.info("immgrationActivityData error, clz not exist, playerId:{}, activityId:{}, data:{}, type:{}", playerId, activityId, data, type.name());
					continue;
				}
				HawkDBEntity entity = clz.newInstance();
				entity.setPersistable(true);
				if (!entity.parseFrom(data)) {
					logger.info("immgrationActivityData error, parse error, playerId:{}, activityId:{}, data:{}, type:{}", playerId, activityId, data, type.name());
					continue;
				}			
				entity.afterRead();
				entity.create();
				logger.info("immgration activity data success, playerId:{}, activityId:{}, data:{}, type:{}", playerId, activityId, data, type.name());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 迁服处理(A迁B，B返回，这里A接收B的返回)
	 * @param protocol
	 */
	@SuppressWarnings("deprecation")
	public void onImmgrationResp(HawkProtocol protocol) {
		ProxyHeader proxyHeader = (ProxyHeader)protocol.getUserData();
		String tarMainServerId = proxyHeader.getFrom();
		String playerId = proxyHeader.getSource();
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		String puid = player.getPuid();
		
		logger.info("player immgration, onImmgrationResp, start, playerId:{}", playerId);
		
		ImmgrationServerResp resp = protocol.parseProtocol(ImmgrationServerResp.getDefaultInstance());
		int code = resp.getCode();
		String tarServerId = resp.getTarServerId();
		
		logger.info("player immgration, onImmgrationResp, start, playerId:{}, code:{}", playerId, code);
		
		if (code == 0) {
			
			// 添加最近登录
			RedisProxy.getInstance().updateRecentServer(tarServerId, player.getOpenId(), player.getPlatform());
			RedisProxy.getInstance().deleRecentServer(player.getServerId(), player.getOpenId(), player.getPlatform());
			
			/**
			 * 移民钻石处理
			 */
			int diamonds = player.getDiamonds();
			if (diamonds > 0) {
				RedisProxy.getInstance().updateImmgrationDiamonds(player.getId() + ":" + tarMainServerId, diamonds);
				// 把原服的钻石消耗掉
				try {
					player.consumeDiamonds(diamonds, Action.IMMGRATION_DIAMONDS, null);	
				} catch (Exception e) {
					HawkException.catchException(e);
				}
				
			}
			
			// 踢下线
			if (player.isActiveOnline()) {
				player.kickout(Status.Immgration.IMMGRATION_KICK_OUT_VALUE, true, null);
			}
			
			// 设置禁止登录
			RedisProxy.getInstance().updateImmgrationBanLogin(player.getOpenId(), player.getServerId(), tarServerId);
			
			// 清除排行榜数据
			RankService.getInstance().dealMsg(MsgId.PERSONAL_RANK_BAN, new BanPersonalRankMsgInvoker(player.getId()));
	
			// 清除城点
			int pointId = WorldPlayerService.getInstance().getPlayerPos(player.getId());
			WorldPointService.getInstance().removeWorldPoint(pointId, true);
			
			// 删除本地所有数据
			PlayerDataSerializer.deleteAllEntity(playerId);

			// 添加目标服的accountRoleInfo
			AccountRoleInfo accountRoleInfo = GlobalData.getInstance().getAccountRoleInfo(player.getId());
			accountRoleInfo.serverId(tarServerId);
			RedisProxy.getInstance().addAccountRole(accountRoleInfo);
			
			// 清除账号信息
			GlobalData.getInstance().removeAccountInfo(player.getId());
			RedisProxy.getInstance().removeAccountRole(player.getOpenId(), player.getServerId(), player.getPlatform());
			
			// cache移除
			GlobalData.getInstance().removeCacheEntity(playerId);
			
			// 活动排行榜数据清理
			clearActivityInfo(playerId);
		}
		
		// 返回给客户端
		ImmgrationServerResp.Builder builder = ImmgrationServerResp.newBuilder();
		builder.setCode(code);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.IMMGRATION_SERVER_RESP, builder));
		
		logger.info("player immgration, onImmgrationResp, finish, playerId:{}, code:{}", playerId, code);

		// 记录一下
		int termId = getImmgrationActivityTermId();
		JSONObject immgrationLog = new JSONObject();
		immgrationLog.put("playerId", playerId);
		immgrationLog.put("fromServer", player.getServerId());
		immgrationLog.put("tarServer", tarServerId);
		immgrationLog.put("time", HawkTime.formatNowTime());
		immgrationLog.put("puid", puid);
		RedisProxy.getInstance().addImmgrationLog(termId, immgrationLog);
	}
	
	/**
	 * 活动排行榜数据清理
	 */
	private void clearActivityInfo(String playerId) {
		HawkLog.logPrintln("immgrationService remove player rank info start, playerId: {}", playerId);
		
		//能量源收集、铁血军团、全军动员、......（17是尤里复仇）
		//List<Integer> activityList = Arrays.asList(15,25,26,40,76,89,93,103,104,163,275,284,317,345,347,351);
		
		YuriRevengeService.getInstance().removeRank(playerId); //17是尤里复仇
		for (ActivityType activityType : ActivityType.values()) {
			try {
				Optional<ActivityBase> activityOp = ActivityManager.getInstance().getGameActivityByType(activityType.getNumber());
				if (activityOp.isPresent()) {
					activityOp.get().removePlayerRank(playerId);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		HawkLog.logPrintln("immgrationService remove player rank info end, playerId: {}", playerId);
	}
	
	/**
	 * 迁服活动是否开启
	 * @return
	 */
	public boolean isImmgrationActivityOpen() {
		Optional<ActivityBase> activity = ActivityManager.getInstance().getGameActivityByType(ActivityType.IMMGRATION_VALUE);
		if (!activity.isPresent()) {
			return false;
		}
		
		ActivityState state = activity.get().getActivityEntity().getActivityState();
		return state == ActivityState.OPEN || state == ActivityState.SHOW;
	}
	
	/**
	 * 获取迁服活动期数
	 * @return
	 */
	public int getImmgrationActivityTermId() {
		if (!isImmgrationActivityOpen()) {
			return 0;
		}
		Optional<ActivityBase> activity = ActivityManager.getInstance().getGameActivityByType(ActivityType.IMMGRATION_VALUE);
		return activity.get().getActivityTermId();
	}
	
	/**
	 * 计算迁服消耗道具数量
	 * @return
	 */
	public int calcImmgrationCostItemCount(Player player, String tarServerId) {
		int itemCount = 0;
		
		int ownServerRank = 0;
		RankInfo ownServerRankInfo = RankService.getInstance().getRankInfo(RankType.PLAYER_NOARMY_POWER_RANK, player.getId());
		if (ownServerRankInfo != null) {
			ownServerRank = ownServerRankInfo.getRank();
		}
		itemCount += getItemUseCfg(ownServerRank).getLocalNumber();
		
		int otherServerRank = RankService.getInstance().getOtherServerNoArmyPowerRank(tarServerId, player.getNoArmyPower());
		itemCount += getItemUseCfg(otherServerRank).getTargetNumber();
		
		if (itemCount == 0) {
			ImmgrationActivityKVCfg constCfg = HawkConfigManager.getInstance().getKVInstance(ImmgrationActivityKVCfg.class);
			itemCount = constCfg.getItemNumMin();
		}
		return itemCount;
	}
	
	/**
	 * 获取迁服道具数量配置
	 * @param rank
	 * @return
	 */
	private ImmgrationItemUseCfg getItemUseCfg(int rank) {
		ImmgrationItemUseCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ImmgrationItemUseCfg.class, rank);
		if (cfg == null) {
			int configSize = HawkConfigManager.getInstance().getConfigSize(ImmgrationItemUseCfg.class);
			cfg = HawkConfigManager.getInstance().getConfigByIndex(ImmgrationItemUseCfg.class, configSize - 1);
		}
		return cfg;
	}
	
	
	private void addBackImmgrationNotice(String playerId,String playerName){
		String serverId = GsConfig.getInstance().getServerId();
		Optional<ActivityBase> activity = ActivityManager.getInstance().getGameActivityByType(ActivityType.BACK_IMMGRATION_VALUE);
		if(!activity.isPresent()){
			return;
		}
		BackImmgrationActivity backActivity = (BackImmgrationActivity) activity.get();
		int termId = backActivity.getActivityTermId();
		String rlt = RedisProxy.getInstance().getBackImmgrationRecord(termId, playerId);
		if(HawkOSOperator.isEmptyString(rlt)){
			HawkLog.logPrintln("immgrationService addBackImmgrationNotice rlt null, playerId: {}", playerId);
			return;
		}
		JSONObject rltObj = JSONObject.parseObject(rlt);
		if(!rltObj.containsKey("tarServer")){
			HawkLog.logPrintln("immgrationService addBackImmgrationNotice rlt tarServer null, playerId: {},str:{}", playerId,rlt);
			return;
		}
		String tarServer = rltObj.getString("tarServer");
		if(!tarServer.equals(serverId)){
			HawkLog.logPrintln("immgrationService addBackImmgrationNotice rlt tarServer er r, playerId: {},str:{}", playerId,rlt);
			return;
		}
		//跑马灯
		Const.NoticeCfgId noticeId = Const.NoticeCfgId.PLAYER_IMMIGRATE_IN;
		ChatParames chatParams = ChatParames.newBuilder()
				.setChatType(Const.ChatType.SPECIAL_BROADCAST)
				.setKey(noticeId)
				.addParms(playerName)
				.build();
		ChatService.getInstance().addWorldBroadcastMsg(chatParams);
	}
}

package com.hawk.game.module;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.impl.backFlow.comm.BackFlowPlayer;
import com.hawk.activity.type.impl.backImmigration.BackImmgrationActivity;
import com.hawk.activity.type.impl.backImmigration.BackImmgrationData;
import com.hawk.activity.type.impl.immgration.cfg.ImmgrationActivityKVCfg;
import com.hawk.activity.type.impl.immgration.cfg.ImmgrationActivityTimeCfg;
import com.hawk.activity.type.impl.lotteryTicket.LotteryTicketActivity;
import com.hawk.common.AccountRoleInfo;
import com.hawk.common.ServerInfo;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.activity.impl.backflow.BackFlowService;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.ImmgrationActivityCfg;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.cache.PlayerDataSerializer;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Immgration.ImmgrationActivityInfo;
import com.hawk.game.protocol.Immgration.ImmgrationActivityInfoResp;
import com.hawk.game.protocol.Immgration.ImmgrationServerInfo;
import com.hawk.game.protocol.Immgration.ImmgrationServerInfoDetailResp;
import com.hawk.game.protocol.Immgration.ImmgrationServerReq;
import com.hawk.game.protocol.Immgration.ImmgrationServerType;
import com.hawk.game.protocol.Immgration.PBBackImmgrationActionReq;
import com.hawk.game.protocol.Immgration.PBBackImmgrationServerListResp;
import com.hawk.game.protocol.Immgration.PBBackImmgrationTargetServerRankReq;
import com.hawk.game.protocol.Immgration.PBCrossBackImmgrationTargetServerRankReq;
import com.hawk.game.rank.RankService;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.ImmgrationService;
import com.hawk.game.service.RelationService;
import com.hawk.game.world.WorldMarchService;
import com.hawk.log.Action;

/**
 * 移民(迁服)
 * @author Golden
 *
 */
public class PlayerImmgrationModule extends PlayerModule {
	
	/**
	 * 日志
	 */
	protected static final Logger logger = LoggerFactory.getLogger("Server");
	
	/**
	 * 构造函数
	 * @param player
	 */
	public PlayerImmgrationModule(Player player) {
		super(player);
	}

	@Override
	public boolean onTick() {
		return true;
	}
	
	/**
	 * 活动信息请求
	 */
	@ProtocolHandler(code = HP.code2.IMMGRATION_ACTIVITY_INFO_REQ_VALUE)
	private void onActivityInfoReq(HawkProtocol protocol) {
		if (!ImmgrationService.getInstance().isImmgrationActivityOpen()) {
			return;
		}
		syncActivityInfo();
	}

	/**
	 * 同步界面信息
	 */
	private void syncActivityInfo() {
		int activityTermId = ImmgrationService.getInstance().getImmgrationActivityTermId();
		ImmgrationActivityInfo.Builder builder = ImmgrationActivityInfo.newBuilder();
		builder.setCurrentNum(0);
		builder.setNumLimit(0);
		builder.setCurrentPower(0);
		builder.setPowerLimit(0);
		builder.setPowerMinLimit(0);
		boolean alreadyTrue = RedisProxy.getInstance().isPlayerImmgration(activityTermId, player.getId());
		builder.setAlreadyImmgration(alreadyTrue);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.IMMGRATION_ACTIVITY_INFO_RESP, builder));
	}
	
	/**
	 * 区服列表信息请求
	 */
	@ProtocolHandler(code = HP.code2.IMMGRATION_SERVER_INFO_REQ_VALUE)
	private void onServerListInfoReq(HawkProtocol protocol) {
		if (!ImmgrationService.getInstance().isImmgrationActivityOpen()) {
			return;
		}
		// 可以迁服的服务器
		List<ServerInfo> canImmgrationServerList = getCanImmgrationServer();
		ImmgrationActivityInfoResp.Builder builder = ImmgrationActivityInfoResp.newBuilder();
		for (ServerInfo serverInfo : canImmgrationServerList) {
			try {
				ImmgrationServerInfo.Builder serverInfoBuilder = ImmgrationServerInfo.newBuilder();
				serverInfoBuilder.setServerId(serverInfo.getId());
				serverInfoBuilder.setServerName(serverInfo.getName());
				serverInfoBuilder.setServerType(ImmgrationServerType.CAN_IMMGRATION);
				builder.addServerInfo(serverInfoBuilder);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		// 自己有账号的服
		Map<String, AccountRoleInfo> recentServer = GlobalData.getInstance().getPlayerAccountInfos(player.getOpenId());
		Map<String, ServerInfo> serverMap = new HashMap<>(GlobalData.getInstance().getServerMap());
		for (AccountRoleInfo accountRoleInfo : recentServer.values()) {
			try {
				ImmgrationServerInfo.Builder serverInfoBuilder = ImmgrationServerInfo.newBuilder();
				serverInfoBuilder.setServerId(accountRoleInfo.getServerId());
				ServerInfo serverInfo = serverMap.get(accountRoleInfo.getServerId());
				if (serverInfo != null) {
					serverInfoBuilder.setServerName(serverInfo.getName());
				}
				if (serverInfo.getId().equals(GsConfig.getInstance().getServerId())) {
					serverInfoBuilder.setServerType(ImmgrationServerType.MAIN_SERVER);
				} else {
					serverInfoBuilder.setServerType(ImmgrationServerType.OWN_SERVER);
				}
				serverInfoBuilder.setIcon(accountRoleInfo.getIcon());
				serverInfoBuilder.setPfIcon(accountRoleInfo.getPfIcon());
				serverInfoBuilder.setCityLevel(accountRoleInfo.getCityLevel());
				builder.addServerInfo(serverInfoBuilder);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.IMMGRATION_SERVER_INFO_RESP, builder));
	}

	/**
	 * 获取可以迁移的目标服
	 * @return
	 */
	private List<ServerInfo> getCanImmgrationServer() {
		List<ServerInfo> canImmgrationServerList = new ArrayList<>();
		
		// 开服时间
		long serverOpenTime = GsApp.getInstance().getServerOpenTime();
		// 最近登陆的区服
		Map<String, AccountRoleInfo> recentServer = GlobalData.getInstance().getPlayerAccountInfos(player.getOpenId());
		// 配置
		ImmgrationActivityKVCfg constCfg = HawkConfigManager.getInstance().getKVInstance(ImmgrationActivityKVCfg.class);
		
		Map<String, ServerInfo> serverMap = new HashMap<>(GlobalData.getInstance().getServerMap());
		for (ServerInfo serverInfo : serverMap.values()) {
			// 只找普通服
			if (serverInfo.getServerType() != 0) {
				continue;
			}
			// 过滤掉本服
			if (serverInfo.getId().equals(GsConfig.getInstance().getServerId())) {
				continue;
			}
			// 过滤掉合服的从服
			if (!GlobalData.getInstance().isMainServer(serverInfo.getId())) {
				continue;
			}
			// 自己有账号的服过滤掉
			if (recentServer.containsKey(serverInfo.getId())) {
				continue;
			}
			// 判断从服是否有号
			boolean hasMerge = false;
			List<String> mergeServerList = GlobalData.getInstance().getMergeServerList(serverInfo.getId());
			if (mergeServerList != null) {
				for (String mergerServer : mergeServerList) {
					if (recentServer.containsKey(mergerServer)) {
						hasMerge = true;
					}
				}
				if (hasMerge) {
					continue;
				}
			}
			
			// 开服时间不足
			if (HawkTime.getMillisecond() - HawkTime.parseTime(serverInfo.getOpenTime()) < constCfg.getServerDelay()) {
				continue;
			}
			// 新服保护
			if (HawkTime.getMillisecond() - HawkTime.parseTime(serverInfo.getOpenTime()) < constCfg.getNewServerProtect()) {
				continue;
			}
			// 开服间隔天数
			if (getBetweenDays(serverOpenTime, HawkTime.parseTime(serverInfo.getOpenTime())) > constCfg.getMigrantDay()) {
				continue;
			}
			canImmgrationServerList.add(serverInfo);
		}
		return canImmgrationServerList;
	}
	
	/**
	 * 获取间隔天数
	 * @param time1
	 * @param time2
	 * @return
	 */
	public int getBetweenDays(long time1, long time2){
		Date date1 = null;
		Date date2 = null;
		if (time1 < time2) {
			date1 = new Date(time1);
			date2 = new Date(time2);
		} else {
			date1 = new Date(time2);
			date2 = new Date(time1);
		}
		return HawkTime.calcBetweenDays(date1, date2);
	}
	
	/**
	 * 区服信息请求
	 */
	@ProtocolHandler(code = HP.code2.IMMGRATION_SERVER_DETAIL_INFO_REQ_VALUE)
	private void onServerInfoReq(HawkProtocol protocol) {
		if (!ImmgrationService.getInstance().isImmgrationActivityOpen()) {
			return;
		}
		
		ImmgrationServerReq req = protocol.parseProtocol(ImmgrationServerReq.getDefaultInstance());
		String tarServerId = req.getTarServerId();
		
		ImmgrationServerInfoDetailResp.Builder builder = ImmgrationServerInfoDetailResp.newBuilder();
		builder.setServerId(tarServerId);
		ServerInfo serverInfo = GlobalData.getInstance().getServerInfo(tarServerId);
		builder.setServerName(serverInfo.getName());
		builder.setServerType(ImmgrationServerType.CAN_IMMGRATION);
		builder.setNumLimit(0);
		builder.setPowerLimit(0);
		builder.setCurrentNum(0);
		builder.setImmgrationCost(ImmgrationService.getInstance().calcImmgrationCostItemCount(player, tarServerId));
		
		// 发到目标服去取排行榜
		HawkProtocol sendProto = HawkProtocol.valueOf(HP.code2.IMMGRATION_SERVER_DETAIL_INFO_RESP, builder);
		CrossProxy.getInstance().sendNotify(sendProto, tarServerId, player.getId(), null);		
	}
	
	/**
	 * 真正的迁服
	 */
	@ProtocolHandler(code = HP.code2.IMMGRATION_SERVER_REQ_VALUE)
	private void onImmgration(HawkProtocol protocol) {
		if (!ImmgrationService.getInstance().isImmgrationActivityOpen()) {
			logger.info("player immgration, onImmgration, activity not open, playerId:{}", player.getId());
			return;
		}
		
		ImmgrationServerReq req = protocol.parseProtocol(ImmgrationServerReq.getDefaultInstance());
		String tarServerId = req.getTarServerId();
		
		// 回流检查
		if (!checkOpenBackFlow()) {
			logger.info("player immgration, onImmgration, checkOpenBackFlow, playerId:{}", player.getId());
			return;
		}
		
		// 迁服前检查
		if (!checkBeforeImmgration(tarServerId)) {
			logger.info("player immgration, onImmgration, checkBeforeImmgration, playerId:{}", player.getId());
			return;
		}
		
		// 检测道具消耗
		int immgrationCost = ImmgrationService.getInstance().calcImmgrationCostItemCount(player, tarServerId);
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		ImmgrationActivityKVCfg constCfg = HawkConfigManager.getInstance().getKVInstance(ImmgrationActivityKVCfg.class);
		consumeItems.addItemConsume(constCfg.getItem(), immgrationCost);
		if (!consumeItems.checkConsume(player)) {
			logger.info("player immgration, onImmgration, consume failed, playerId:{}", player.getId());
			return;
		}
		consumeItems.consumeAndPush(player, Action.IMMGRATION);
		
		logger.info("player immgration, onImmgration, flush to redis begin, playerId:{}", player.getId());
		
		try {
			int termId = ImmgrationService.getInstance().getImmgrationActivityTermId();
			JSONObject immgrationLog = new JSONObject();
			immgrationLog.put("playerId", player.getId());
			immgrationLog.put("fromServer", GsConfig.getInstance().getServerId());
			immgrationLog.put("tarServer", tarServerId);
			immgrationLog.put("time", HawkTime.formatNowTime());
			immgrationLog.put("puid", player.getPuid());
			RedisProxy.getInstance().updateImmgrationRecord(termId, player.getId(), immgrationLog.toJSONString());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		//把数据刷到redis里面
		try {
			boolean flushToRedis = PlayerDataSerializer.flushToRedis(player.getData().getDataCache(), null, false);
			if (!flushToRedis) {
				return;
			}
			
			// 序列化活动数据
			ConfigIterator<ImmgrationActivityCfg> cfgIter = HawkConfigManager.getInstance().getConfigIterator(ImmgrationActivityCfg.class);
			while (cfgIter.hasNext()) {
				ImmgrationActivityCfg cfg = cfgIter.next();
				if (flushActivityToRedis(player.getId(), cfg.getActivityId())) {
					logger.info("player immgration, onImmgration, flush activity to redis, playerId:{}, activityId:{}", player.getId(), cfg.getActivityId());
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return;
		}
		
		logger.info("player immgration, onImmgration, flush to redis end, playerId:{}", player.getId());
		
		// 通知目标服
		CrossProxy.getInstance().sendNotify(protocol, tarServerId, player.getId(), null);
		
		logger.info("player immgration, onImmgration, send notify, playerId:{}, tarServerId:{}", player.getId(), tarServerId);
	}
	
	public boolean flushActivityToRedis(String playerId, int activityId) {
		// 判断活动存在
		Optional<ActivityBase> activityOp = ActivityManager.getInstance().getActivity(activityId);
		if (!activityOp.isPresent()) {
			logger.info("flush activity to redis, activity not exist, playerId:{}, activityId:{}", player.getId(), activityId);
			return false;
		}
		ActivityBase activity = activityOp.get();
		if (activity.isHidden(playerId)) {
			logger.info("flush activity to redis, activity is hidden, playerId:{}, activityId:{}", player.getId(), activityId);
			return false;
		}
		Optional<HawkDBEntity> entitiyOp = activity.getPlayerDataEntity(playerId);
		if (!entitiyOp.isPresent()) {
			logger.info("flush activity to redis, entity not exist, playerId:{}, activityId:{}", player.getId(), activityId);
			return false;
		}
		
		HawkDBEntity entity = entitiyOp.get();
		try {
			entity.beforeWrite();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		// 序列化存储
		try {
			logger.info("flush activity to redis begin:{}", entity.toString());
			String redisKey = "player_data_activity:" + playerId;
			HawkRedisSession redisSession = RedisProxy.getInstance().getRedisSession();
			redisSession.hSetBytes(redisKey, String.valueOf(activityId), entity.serialize());	
		} catch (Exception e) {
			logger.error("flush activity to redis error, playerId:{}, activityId:{}", player.getId(), activityId);
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	/**
	 * 迁服之前检查
	 */
	public boolean checkBeforeImmgration(String tarServerId) {
		// 有联盟不能移民
		if (player.hasGuild()) {
			sendError(HP.code2.IMMGRATION_SERVER_REQ_VALUE, Status.Immgration.IMMGRATION_HAS_GUILD_VALUE);
			return false;
		}
		// 有军事学院不能移民
		if (player.hasCollege()) {
			sendError(HP.code2.IMMGRATION_SERVER_REQ_VALUE, Status.Immgration.IMMGRATION_HAS_COLLEGE_VALUE);
			return false;
		}
		// 有行军不能移民
		if (WorldMarchService.getInstance().getPlayerMarchCount(player.getId()) > 0) {
			sendError(HP.code2.IMMGRATION_SERVER_REQ_VALUE, Status.Immgration.IMMGRATION_HAS_MARCH_VALUE);
			return false;
		}
		// 有建造中的建筑队列，不能移民
		if (player.getData().getQueueEntitiesByType(QueueType.BUILDING_QUEUE_VALUE).size() > 0) {
			sendError(HP.code2.IMMGRATION_SERVER_REQ_VALUE, Status.Immgration.IMMGRATION_HAS_BUILD_QUEUE_VALUE);
			return false;
		}
		// 有研究中的科技，不能移民
		if (player.getData().getQueueEntitiesByType(QueueType.SCIENCE_QUEUE_VALUE).size() > 0) {
			sendError(HP.code2.IMMGRATION_SERVER_REQ_VALUE, Status.Immgration.IMMGRATION_HAS_SCIENCE_QUEUE_VALUE);
			return false;
		}
		// 正在造兵，不能移民
		if (player.getData().getQueueEntitiesByType(QueueType.SOILDER_QUEUE_VALUE).size() > 0) {
			sendError(HP.code2.IMMGRATION_SERVER_REQ_VALUE, Status.Immgration.IMMGRATION_HAS_SOLIDER_QUEUE_VALUE);
			return false;
		}
		// 有治疗中的兵，不能移民
		if (player.getData().getQueueEntitiesByType(QueueType.CURE_QUEUE_VALUE).size() > 0) {
			sendError(HP.code2.IMMGRATION_SERVER_REQ_VALUE, Status.Immgration.IMMGRATION_HAS_CURE_QUEUE_VALUE);
			return false;
		}
		// 装备科技研究中，不能移民
		if (player.getData().getQueueEntitiesByType(QueueType.EQUIP_RESEARCH_QUEUE_VALUE).size() > 0) {
			sendError(HP.code2.IMMGRATION_SERVER_REQ_VALUE, Status.Immgration.IMMGRATION_HAS_EQUIP_RECHARGE_QUEUE_VALUE);
			return false;
		}
		// 有守护关系，不能移民
		if (RelationService.getInstance().hasGuarder(player.getId())) {
			sendError(HP.code2.IMMGRATION_SERVER_REQ_VALUE, Status.Immgration.IMMGRATION_HAS_GUARDER_VALUE);
			return false;
		}
		// 玩家处于跨服状态中，不能移民
		if (player.isCsPlayer()) {
			sendError(HP.code2.IMMGRATION_SERVER_REQ_VALUE, Status.Immgration.IMMGRATION_CROSS_VALUE);
			return false;
		}
		// 我在目标服有账号
		Map<String, AccountRoleInfo> accountRoleInfos = GlobalData.getInstance().getPlayerAccountInfos(player.getOpenId());
		for (AccountRoleInfo accountRole : accountRoleInfos.values()) {
			if (accountRole.getServerId().equals(tarServerId)) {
				sendError(HP.code2.IMMGRATION_SERVER_REQ_VALUE, Status.Immgration.IMMGRATION_TARGET_HAS_ACCOUNT_VALUE);
				return false;
			}
		}
		//有未处理的活动数据
		boolean lotteryTicketLimit = this.checkLotteryTicketActivityDataLimit(player.getId());
		if(lotteryTicketLimit){
			sendError(HP.code2.IMMGRATION_SERVER_REQ_VALUE, Status.Immgration.IMMGRATION_LOTTERY_TICKET_APPLY_NEED_DEAL_VALUE);
			return false;
		}
		return true;
	}
	
	/**
	 * 检测是否开启回流
	 */
	public boolean checkOpenBackFlow() {
		// 配置
		ImmgrationActivityKVCfg constCfg = HawkConfigManager.getInstance().getKVInstance(ImmgrationActivityKVCfg.class);
		
		// 注册天试
		if (player.getPlayerRegisterDays() < constCfg.getRegisterDay()) {
			logger.info("player immgration, checkOpenBackFlow, register days, playerId:{}, param:{}", player.getId(), player.getPlayerRegisterDays());
			return false;
		}
		
		// 是否是回流玩家
		BackFlowPlayer backFlowPlayer = BackFlowService.getInstance().getBackFlowPlayer(player.getId());
		if (backFlowPlayer == null) {
			logger.info("player immgration, backFlowPlayer null, playerId:{}", player.getId());
			return false;
		}
		
		// 流失天数
		if (backFlowPlayer.getLossDays() < constCfg.getLostDay()) {
			logger.info("player immgration, checkOpenBackFlow, loss days, playerId:{}, param:{}", player.getId(), backFlowPlayer.getLossDays());
			return false;
		}
		
		// 是否在x时间之后活跃过
		long activeTime = HawkTime.parseTime(constCfg.getActiveTimeStart());
		if (backFlowPlayer.getLogoutTime() < activeTime) {
			logger.info("player immgration, checkOpenBackFlow, logout, playerId:{}, param:{}", player.getId(), backFlowPlayer.getLogoutTime());
			return false;
		}
		
		int vipLevel = player.getVipLevel();
		// vip等级判断
		if (vipLevel < constCfg.getVipLevel()) {
			logger.info("player immgration, checkOpenBackFlow, vip, playerId:{}, param:{}", player.getId(), vipLevel);
			return false;
		}

		// 活动不存在
		Optional<ActivityBase> activityOp = ActivityManager.getInstance().getGameActivityByType(ActivityType.IMMGRATION_VALUE);
		if (!activityOp.isPresent()) {
			return false;
		}
		ActivityBase activity = activityOp.get();
		
		// 非活动期间回流
		ImmgrationActivityTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(ImmgrationActivityTimeCfg.class, activity.getActivityTermId());
		if (timeCfg == null) {
			logger.info("player immgration, checkOpenBackFlow, time null, playerId:{}", player.getId());
			return false;
		}
		
		if (backFlowPlayer.getBackTimeStamp() < timeCfg.getStartTimeValue() || backFlowPlayer.getBackTimeStamp() > timeCfg.getEndTimeValue()) {
			logger.info("player immgration, checkOpenBackFlow, time not in, playerId:{}", player.getId());
			return false;
		}

		// 灰度区服判断
		List<String> openServerList = constCfg.getOpenServerList();
		if (!openServerList.isEmpty() && !openServerList.contains(GsConfig.getInstance().getServerId())) {
			logger.info("player immgration, checkOpenBackFlow, openServerList, playerId:{}", player.getId());
			return false;
		}
		
		return true;
	}
	
	/**
	 * 活动是否开启
	 * @return
	 */
	public boolean isActivityOpen() {
		Optional<ActivityBase> activity = ActivityManager.getInstance().getGameActivityByType(ActivityType.MACHINE_AWAKE_TWO_VALUE);
		return activity.isPresent() && activity.get().getActivityEntity().getActivityState() == ActivityState.OPEN;
	}
	
	
	//*******回流移民************************************
	
	@ProtocolHandler(code = HP.code2.BACK_IMMGRATION_SERVER_LIST_REQ_VALUE)
	private void onBackImmgrationServerListReq(HawkProtocol protocol) {
		if(player.isCsPlayer()){
			return;
		}
		if(player.isInDungeonMap()){
			return;
		}
		String playerId = this.player.getId();
		BackImmgrationActivity activity = this.getBackImmgrationActivity(playerId);
		if(Objects.isNull(activity)){
			return;
		}
		if(activity.isActivityClose(playerId)){
			return;
		}
		List<String> list = activity.getPlayerImmgrationServer(playerId);
		PBBackImmgrationServerListResp.Builder builder = PBBackImmgrationServerListResp.newBuilder();
		builder.addAllServers(list);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.BACK_IMMGRATION_SERVER_LIST_RESP_VALUE, builder));
	}
	
	//去目的服拿排行榜
	@ProtocolHandler(code = HP.code2.BACK_IMMGRATION_SERVER_RANK_REQ_VALUE)
	private void onBackImmgrationServerRankReq(HawkProtocol protocol) {
		PBBackImmgrationTargetServerRankReq req = protocol.parseProtocol(PBBackImmgrationTargetServerRankReq.getDefaultInstance());
		String playerId = this.player.getId();
		if(player.isCsPlayer()){
			return;
		}
		if(player.isInDungeonMap()){
			return;
		}
		BackImmgrationActivity activity = this.getBackImmgrationActivity(playerId);
		if(Objects.isNull(activity)){
			return;
		}
		if(activity.isActivityClose(playerId)){
			return;
		}
		String tarServerId = req.getServerId();
		
		BuildingCfg buildingCfg = player.getData().getBuildingCfgByType(BuildingType.CONSTRUCTION_FACTORY);
		int cityScore = buildingCfg.getLevel();
		int honor = buildingCfg.getHonor();
		int progress = buildingCfg.getProgress();
		if (honor > 0 || progress > 0) {
			cityScore = buildingCfg.getLevel() * RankService.HONOR_CITY_OFFSET + progress;
		}
		int power = this.player.getNoArmyPower();
		PBCrossBackImmgrationTargetServerRankReq.Builder builder = PBCrossBackImmgrationTargetServerRankReq.newBuilder();
		builder.setPlayerId(playerId);
		builder.setPower(power);
		builder.setCityLevel(cityScore);
		// 发到目标服去取排行榜
		HawkProtocol sendProto = HawkProtocol.valueOf(HP.code2.BACK_CROSS_IMMGRATION_SERVER_RANK_REQ_VALUE, builder);
		CrossProxy.getInstance().sendNotify(sendProto, tarServerId, player.getId(), null);		
	}
	
	
	
	@ProtocolHandler(code = HP.code2.BACK_IMMGRATION_ACTION_REQ_VALUE)
	private void onBackImmgrationActionReq(HawkProtocol protocol) {
		String playerId = this.player.getId();
		if(player.isCsPlayer()){
			return;
		}
		if(player.isInDungeonMap()){
			return;
		}
		BackImmgrationActivity activity = this.getBackImmgrationActivity(playerId);
		if(Objects.isNull(activity)){
			return;
		}
		if(activity.isActivityClose(playerId)){
			return;
		}
		PBBackImmgrationActionReq req = protocol.parseProtocol(PBBackImmgrationActionReq.getDefaultInstance());
		String tarServerId = req.getTarServerId();
		List<String> serverList = activity.getPlayerImmgrationServer(playerId);
		if(!serverList.contains(tarServerId)){
			logger.info("player immgration, onImmgration, checkOpenBackFlow, playerId:{}", player.getId());
			return;
		}
		// 迁服前检查
		if (!checkBeforeImmgration(tarServerId)) {
			logger.info("player immgration, onImmgration, checkBeforeImmgration, playerId:{}", player.getId());
			return;
		}
		//活动数据处理一下
		activity.onPlayerImmgrationAction(playerId, tarServerId);
		try {
			int termId = activity.getActivityTermId();
			JSONObject immgrationLog = new JSONObject();
			immgrationLog.put("playerId", player.getId());
			immgrationLog.put("fromServer", GsConfig.getInstance().getServerId());
			immgrationLog.put("tarServer", tarServerId);
			immgrationLog.put("time", HawkTime.formatNowTime());
			immgrationLog.put("puid", player.getPuid());
			RedisProxy.getInstance().updateBackImmgrationRecord(termId, player.getId(), immgrationLog.toJSONString());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		//把数据刷到redis里面
		try {
			boolean flushToRedis = PlayerDataSerializer.flushToRedis(player.getData().getDataCache(), null, false);
			if (!flushToRedis) {
				return;
			}
			// 序列化活动数据
			ConfigIterator<ImmgrationActivityCfg> cfgIter = HawkConfigManager.getInstance().getConfigIterator(ImmgrationActivityCfg.class);
			while (cfgIter.hasNext()) {
				ImmgrationActivityCfg cfg = cfgIter.next();
				if (flushActivityToRedis(player.getId(), cfg.getActivityId())) {
					logger.info("player immgration, onImmgration, flush activity to redis, playerId:{}, activityId:{}", player.getId(), cfg.getActivityId());
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return;
		}
		
		logger.info("player immgration, onImmgration, flush to redis end, playerId:{}", player.getId());
		
		// 通知目标服
		ImmgrationServerReq.Builder builder = ImmgrationServerReq.newBuilder();
		builder.setTarServerId(tarServerId);
		builder.setPlayerId(playerId);
		HawkProtocol crossPro = HawkProtocol.valueOf(HP.code2.IMMGRATION_SERVER_REQ_VALUE, builder);
		
		CrossProxy.getInstance().sendNotify(crossPro, tarServerId, player.getId(), null);
		
		logger.info("player immgration, onImmgration, send notify, playerId:{}, tarServerId:{}", player.getId(), tarServerId);
	
	}
	
	
	
	
	
	
	public BackImmgrationActivity getBackImmgrationActivity(String playerId) {
		Optional<ActivityBase> activity = ActivityManager.getInstance().getGameActivityByType(ActivityType.BACK_IMMGRATION_VALUE);
		if(!activity.isPresent()){
			return null;
		}
		BackImmgrationActivity backActivity = (BackImmgrationActivity) activity.get();
		if(!backActivity.isShow(playerId)){
			return null;
		}
		return backActivity;
	} 
	
	public boolean checkLotteryTicketActivityDataLimit(String playerId) {
		Optional<ActivityBase> activity = ActivityManager.getInstance().getGameActivityByType(ActivityType.LOTTERY_TICKET_VALUE);
		if(!activity.isPresent()){
			return false;
		}
		LotteryTicketActivity backActivity = (LotteryTicketActivity) activity.get();
		return backActivity.inApplay(playerId);
	} 
	
}

package com.hawk.activity;

import java.lang.reflect.Method;
import java.util.*;

import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTip;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTipEntity;
import com.hawk.activity.type.impl.seasonActivity.cfg.SeasonConstCfg;
import com.hawk.activity.type.impl.seasonActivity.cfg.SeasonEventCfg;
import com.hawk.activity.type.impl.seasonActivity.cfg.SeasonOpenTimeCfg;
import com.hawk.activity.type.impl.urlReward.IURLReward;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.YQZZWar;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkAppObj;
import org.hawk.asm.HawkAsmMethod;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.msg.HawkMsg;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.configupdate.ActivityConfigUpdateManager;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.monitor.CountMonitor;
import com.hawk.activity.msg.AsyncActivityEventMsg;
import com.hawk.activity.msg.GmCloseActivityMsg;
import com.hawk.activity.msg.GmOpenActivityMsg;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveManager;
import com.hawk.game.protocol.Activity.ConfigNameListReq;
import com.hawk.game.protocol.Activity.SeasonHonorPositionInfo;
import com.hawk.game.protocol.Activity.SeasonMatchType;
import com.hawk.game.protocol.Activity.TakeAchieveRewardMultiReq;
import com.hawk.game.protocol.Activity.TakeAchieveRewardReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.HPOperateSuccess;

/**
 * 活动网络消息集中接收处理，由此类将消息分发至每个活动各自的消息处理句柄中
 * 注：本类对象为单例
 * @author PhilChen
 *
 */
public class ActivityHandler {

	static final Logger logger = LoggerFactory.getLogger("Server");
	
	public static CountMonitor countMonitor = new CountMonitor();

	static Map<Integer, ProtoInvoker> METHOD_MAP = new HashMap<Integer, ProtoInvoker>();
	static Map<Class<? extends HawkMsg>, ProtoInvoker> MSG_METHOD_MAP = new HashMap<Class<? extends HawkMsg>, ProtoInvoker>();

	public class ProtoInvoker {
		private ActivityType activityType;
		private Map<String, HawkAsmMethod> protoVersionMethod;
		private Map<String, HawkAsmMethod> msgVersionMethod;
		private Object handlerObj;

		ProtoInvoker(ActivityType activityType, Method method, Object handlerObj) {
			this.activityType = activityType;
			this.protoVersionMethod = new HashMap<String, HawkAsmMethod>();
			this.msgVersionMethod = new HashMap<String, HawkAsmMethod>();
			this.handlerObj = handlerObj;
		}

		public ActivityType getActivityType() {
			return activityType;
		}

		public HawkAsmMethod getProtoMethod(String version) {
			return protoVersionMethod.get(version);
		}
		
		public HawkAsmMethod getMsgMethod(String version) {
			return msgVersionMethod.get(version);
		}
		
		public void addProtoMethod(String version, HawkAsmMethod method) {
			protoVersionMethod.put(version, method);
		}
		
		public void addMsgMethod(String version, HawkAsmMethod method) {
			msgVersionMethod.put(version, method);
		}
		
		public Map<String, HawkAsmMethod> getProtoVersionMethod() {
			return protoVersionMethod;
		}
		
		public Map<String, HawkAsmMethod> getMsgVersionMethod() {
			return msgVersionMethod;
		}

		public Object getHandlerObj() {
			return handlerObj;
		}
	}

	public ActivityHandler() {
		for (ActivityType activityType : ActivityType.values()) {
			if (activityType.getHandler() == null) {
				continue;
			}
			ActivityProtocolHandler handler = activityType.getHandler();
			scanhandlerMethod(activityType, handler);
		}
		scanhandlerMethod(null, this);
	} 
	
	public static Map<Integer, ProtoInvoker> getProtoMethodMap() {
		return METHOD_MAP;
	} 
	
	public static Map<Class<? extends HawkMsg>, ProtoInvoker> getMessageMethodMap() {
		return MSG_METHOD_MAP;
	}

	/**
	 * 扫描消息句柄的方法映射
	 * @param activityType
	 * @param handler
	 */
	@SuppressWarnings("unchecked")
	private void scanhandlerMethod(ActivityType activityType, Object handler) {
		Method[] declaredMethods = handler.getClass().getDeclaredMethods();
		for (Method method : declaredMethods) {
			if (method.isAnnotationPresent(ProtocolHandler.class)) {
				ProtocolHandler annotation = method.getAnnotation(ProtocolHandler.class);
				int[] codes = annotation.code();
				String version = annotation.version();
				for (int code : codes) {
					ProtoInvoker protoInvoker = new ProtoInvoker(activityType, method, handler);
					HawkAsmMethod asmMethod = HawkAsmMethod.valueOf(handler.getClass(), method);
					protoInvoker.addProtoMethod(version, asmMethod);
					if (METHOD_MAP.containsKey(code)) {
						logger.error("activity protocol handler exist! code: {}", code);
						throw new RuntimeException("activity protocol handler exist! code=" + code);
					}
					METHOD_MAP.put(code, protoInvoker);
				}
			} else if (method.isAnnotationPresent(MessageHandler.class)) {
				MessageHandler annotation = method.getAnnotation(MessageHandler.class);
				Class<?> parameterCls = method.getParameterTypes()[0]; 
				if (parameterCls != HawkMsg.class && HawkMsg.class.isAssignableFrom(parameterCls)) {
					String version = annotation.version();
					ProtoInvoker protoInvoker = new ProtoInvoker(activityType, method, handler);
					HawkAsmMethod asmMethod = HawkAsmMethod.valueOf(handler.getClass(), method);
					protoInvoker.addMsgMethod(version, asmMethod);
					MSG_METHOD_MAP.put((Class<? extends HawkMsg>)parameterCls, protoInvoker);
				} else {
					throw new RuntimeException("MessageHandler annotationed method parameter Type error! it must extends [org.hawk.msg.HawkMsg]" + handler.getClass() + method.toString());
				}
			}
		}
	}
	
	public static boolean onMessage(String playerId, HawkAppObj appObj, HawkMsg msg) {
		ProtoInvoker protoInvoker = MSG_METHOD_MAP.get(msg.getMsgCls());
		if (protoInvoker == null) {
			logger.error("activity protocol handler protoInvoker not found, code: {}, version: {}", msg.getMsgCls(), appObj.getObjVersion());
			return false;
		}
		
		String version = appObj.getObjVersion() == null ? "" : appObj.getObjVersion();
		HawkAsmMethod protoMethod = protoInvoker.getMsgMethod(version);
		if (protoMethod == null) {
			logger.error("activity protocol handler method not found, code: {}, version: {}", msg.getMsgCls(), version);
			return false;
		}
		
		Result<?> result = checkActivityInfo(playerId, protoInvoker);
		if (result.isFail()) {
			return true;
		}
		
		try {
			protoMethod.invoke(protoInvoker.getHandlerObj(), msg, playerId);
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	public static boolean onProtocol(String playerId, HawkAppObj appObj, HawkProtocol protocol) {
		Map<Integer, ProtoInvoker> handlerMap = METHOD_MAP;
		ProtoInvoker protoInvoker = handlerMap.get(protocol.getType());
		if (protoInvoker == null) {
			logger.error("activity protocol handler protoInvoker not found, code: {}, version: {}", protocol.getType(), appObj.getObjVersion());
			return false;
		}
		
		String version = appObj.getObjVersion() == null ? "" : appObj.getObjVersion();
		HawkAsmMethod protoMethod = protoInvoker.getProtoMethod(version);
		if (protoMethod == null) {
			logger.error("activity protocol handler method not found, code: {}, version: {}", protocol.getType(), version);
			return false;
		}
		
		Result<?> result = checkActivityInfo(playerId, protoInvoker);
		if (result.isFail()) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return true;
		}
		
		try {
			protoMethod.invoke(protoInvoker.getHandlerObj(), protocol, playerId);
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}
	
	private static Result<?> checkActivityInfo(String playerId, ProtoInvoker protoInvoker) {
		if (protoInvoker.getActivityType() != null) {
			Optional<ActivityBase> opActivity = ActivityManager.getInstance().getGameActivityByType(protoInvoker.getActivityType().intValue());
			if (!opActivity.isPresent()) {
				return Result.fail(Status.Error.ACTIVITY_NOT_AVAILABLE_VALUE);
			}
			ActivityBase activity = opActivity.get();
			// 活动不允许进行请求操作，默认活动处理开启阶段时才允许操作
			if (activity.isAllowOprate(playerId) == false) {
				return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			}
		}
		return Result.success();
	}
	
	//------------------------------------------------------------------
	/**
	 * 玩家登录事件（player线程直接调用）
	 * @param playerId
	 * @param activityData
	 * @return
	 */
	public static boolean onPlayerLogin(String playerId) {
		return ActivityManager.getInstance().onPlayerLogin(playerId);
	}

	/**
	 * 玩家登出事件（player线程直接调用）
	 * @param playerId
	 * @return
	 */
	public static boolean onPlayerLogout(String playerId) {
		return ActivityManager.getInstance().onPlayerLogout(playerId);
	}

	/**
	 * 玩家tick
	 * @param playerId
	 * @return
	 */
	public static boolean onPlayerTick(String playerId) {
		return ActivityManager.getInstance().onPlayerTick(playerId);
	}
	//==============一些独立的活动网络消息可以写在下面======================
	
	/**
	 * 异步事件执行
	 * @param msg
	 * @param playerId
	 */
	@MessageHandler
	public void onEventMsg(AsyncActivityEventMsg msg, String playerId) {
		ActivityManager.getInstance().onEventMsg(msg, playerId);
	}
	
	@MessageHandler
	public void onGmActivityCloseMsg(GmCloseActivityMsg msg){
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(msg.getActivityId());
		if(opActivity.isPresent()){
			opActivity.get().onGmCloseActivity();
		}
	}
	
	@MessageHandler
	public void onGmActivityOpenMsg(GmOpenActivityMsg msg){
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(msg.getActivityId());
		if(opActivity.isPresent()){
			opActivity.get().onGmOpenActivity();
		}
	}
	
	/**
	 * 拉取活动配置文件
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.PULL_ACTIVITY_CONFIG_C_VALUE)
	public boolean onPullActivityConfigs(HawkProtocol protocol, String playerId) {
		ConfigNameListReq req = protocol.parseProtocol(ConfigNameListReq.getDefaultInstance());
		List<String> cfgNameList = req.getConfigNameList();
		ActivityConfigUpdateManager.getInstance().pushClientCfg(playerId, cfgNameList);
		return true;
	}
	
	/**
	 * 拉取所有活动玩家数据
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.PULL_ALL_ACTIVITY_INFO_VALUE)
	public boolean onPullActivityInfo(HawkProtocol protocol, String playerId) {
		ActivityManager.getInstance().syncAllActivityInfo(playerId);
		return true;
	}
	
	/**
	 * 领取成就类（八日庆典、每日任务、成长基金、王者归来、使命战争）活动奖励
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.TAKE_ACHIEVE_REWARD_C_VALUE)
	public boolean onAchieveReward(HawkProtocol protocol, String playerId) {
		TakeAchieveRewardReq req = protocol.parseProtocol(TakeAchieveRewardReq.getDefaultInstance());
		Result<?> result = AchieveManager.getInstance().takeAchieveReward(playerId, req);
		if (result.isFail()) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			logger.debug("activity take achieve reward failed, playerId: {}, achieveId: {}, errCode: {}", playerId, req.getAchieveId(), result.getStatus());
			return false;
		} else {
			HPOperateSuccess.Builder builder = HPOperateSuccess.newBuilder().setHpCode(protocol.getType());
			HawkProtocol resp = HawkProtocol.valueOf(HP.sys.OPERATE_SUCCESS, builder);
			PlayerPushHelper.getInstance().pushToPlayer(playerId, resp);
		}
		return true;
	}
	
	@ProtocolHandler(code = HP.code2.TAKE_ACHIEVE_REWARD_MULTI_C_VALUE)
	public boolean onAchieveMultiReward(HawkProtocol protocol, String playerId) {
		TakeAchieveRewardMultiReq req = protocol.parseProtocol(TakeAchieveRewardMultiReq.getDefaultInstance());
		Result<?> result = AchieveManager.getInstance().takeAchieveMultiReward(playerId, req);
		if (result.isFail()) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			logger.debug("activity take achieve multi reward failed, playerId: {}, errCode: {}", playerId, result.getStatus());
			return false;
		} else {
			HPOperateSuccess.Builder builder = HPOperateSuccess.newBuilder().setHpCode(protocol.getType());
			HawkProtocol resp = HawkProtocol.valueOf(HP.sys.OPERATE_SUCCESS, builder);
			PlayerPushHelper.getInstance().pushToPlayer(playerId, resp);
		}
		return true;
	}

	/**
	 * 兑换勾选信息
	 * @param protocol 前端数据
	 * @param playerId 玩家id
	 */
	@ProtocolHandler(code = HP.code.GENERAL_EXCHANGE_TIPS_VALUE)
	public void exchange(HawkProtocol protocol, String playerId){
		Activity.GeneralExchangeTipsInfo req = protocol.parseProtocol(Activity.GeneralExchangeTipsInfo.getDefaultInstance());
		int activityId = req.getActivityId();
		ActivityBase activity = null;
		for (ActivityBase base : ActivityManager.getInstance().getActivityMap().values()) {
			if (activityId== base.getActivityCfg().getActivityType()) {
				activity = base;
				break;
			}
		}
		if(activity == null
				|| !(activity instanceof IExchangeTip)
				|| activity.isHidden(playerId)){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol.getType(), Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			return;
		}
		Optional<HawkDBEntity> opDataEntity = activity.getPlayerDataEntity(playerId);
		if(!opDataEntity.isPresent()){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol.getType(), Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
			return;
		}
		HawkDBEntity entity = opDataEntity.get();
		if(!(entity instanceof IExchangeTipEntity)){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol.getType(), Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
			return;
		}
		IExchangeTip<?> exchangeTipActivity = (IExchangeTip<?>) activity;
		Result<?> result = exchangeTipActivity.chooseTip((IExchangeTipEntity) entity, req.getTipsList());
		if(result.isFail()){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
		activity.syncActivityDataInfo(playerId);
	}

	@SuppressWarnings("rawtypes")
	@ProtocolHandler(code = HP.code2.URL_REWARD_GET_VALUE)
	public void urlRewardGet(HawkProtocol protocol, String playerId){
		Activity.URLRewardGet req = protocol.parseProtocol(Activity.URLRewardGet.getDefaultInstance());
		int activityId = req.getActivityId();
		ActivityBase activity = null;
		for (ActivityBase base : ActivityManager.getInstance().getActivityMap().values()) {
			if (activityId== base.getActivityCfg().getActivityType()) {
				activity = base;
				break;
			}
		}
		if(activity == null
				|| !(activity instanceof IURLReward)
				|| activity.isHidden(playerId)){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol.getType(), Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			return;
		}
		IURLReward urlActivity = (IURLReward) activity;
		Result<?> result = urlActivity.getURLReward(playerId, activityId);
		if(result.isFail()){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
		activity.syncActivityDataInfo(playerId);
	}

	@ProtocolHandler(code = HP.code2.SEASON_HONOR_REQ_VALUE)
	public void seasonHonor(HawkProtocol protocol, String playerId){
		Activity.SeasonHonorReq req = protocol.parseProtocol(Activity.SeasonHonorReq.getDefaultInstance());
		int season = req.getSeason();
		SeasonOpenTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(SeasonOpenTimeCfg.class, season);
		if(timeCfg == null){
			return;
		}
		SeasonEventCfg posEventCfg = HawkConfigManager.getInstance().getConfigByKey(SeasonEventCfg.class, season);
		if (posEventCfg == null) {
			return;
		}
		
		String serverId = ActivityManager.getInstance().getDataGeter().getPlayerServerId(playerId);
		int posId = 0, areaId = Integer.parseInt(serverId) / 10000;
		if (areaId != 1 && areaId != 2) {
			areaId = Integer.parseInt(serverId) / 2 == 0 ? 2 : 1;
		}
		Activity.SeasonHonorResp.Builder builder = Activity.SeasonHonorResp.newBuilder();
		builder.setSeason(season);
		for (int posEvent : posEventCfg.getEventList()) {
			try {
				posId++;
				builderSeasonHonorRank(season, posEvent, areaId, timeCfg, builder);
				SeasonHonorPositionInfo.Builder posEventBuilder = SeasonHonorPositionInfo.newBuilder();
				posEventBuilder.setPosId(posId);
				posEventBuilder.setType(SeasonMatchType.valueOf(posEvent));
				builder.addPosInfo(posEventBuilder);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		SeasonConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(SeasonConstCfg.class);
		builder.setTime(cfg.getTime(season));
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.SEASON_HONOR_RESP_VALUE, builder));
	}
	
	/**
	 * 构建赛事排行数据
	 * @param season
	 * @param posEvent
	 * @param areaId
	 * @param timeCfg
	 * @param builder
	 */
	private void builderSeasonHonorRank(int season, int posEvent, int areaId, SeasonOpenTimeCfg timeCfg, Activity.SeasonHonorResp.Builder builder) {
		switch (posEvent) {
			case SeasonMatchType.S_CYBORG_VALUE: //赛伯利亚
				break;
			case SeasonMatchType.S_TBLY_VALUE: //泰伯赛季
			{
				String seasonHonorKey = "SEASON_HONOR:SEASON:" + areaId + ":" + season;
				if (!ActivityManager.getInstance().getDataGeter().isSeasonHonorDataNew()) {
					seasonHonorKey = "SEASON_HONOR:SEASON:" + season;
				}
				List<Activity.SeasonGuildKingRankMsg.Builder> seasonRankList = new ArrayList<>();
				List<byte[]> seasonHonor = ActivityGlobalRedis.getInstance().getRedisSession().lRange(seasonHonorKey.getBytes(), 0, 1000,0);
				for (byte[] bytes : seasonHonor) {
					try {
						Activity.SeasonGuildKingRankMsg.Builder rankBuilder = Activity.SeasonGuildKingRankMsg.newBuilder();
						rankBuilder.mergeFrom(bytes);
						if (season <=3 ) {
							seasonRankList.add(rankBuilder);
						} else {
							seasonRankList.add(0, rankBuilder);
						}
					}catch (Exception e){
						HawkException.catchException(e);
					}
				}
				for(Activity.SeasonGuildKingRankMsg.Builder rankBuilder : seasonRankList){
					builder.addSeasonRank(rankBuilder);
				}
				break;
			}
			case SeasonMatchType.S_DYZZ_VALUE: //陨晶，达雅之战
				break;
			case SeasonMatchType.S_YQZZ_VALUE: //月球之战
			{
				int yqzzTermId = timeCfg.getMatchTermId(Activity.SeasonMatchType.S_YQZZ_VALUE);
				Map<Integer, YQZZWar.PBYQZZLeagueWarServerInfo.Builder> yqzzRankMap = new HashMap<>();
				String yqzzHonorKey = "SEASON_HONOR:YQZZ:" + areaId + ":" + yqzzTermId;
				if (!ActivityManager.getInstance().getDataGeter().isSeasonHonorDataNew()) {
					yqzzHonorKey = "SEASON_HONOR:YQZZ:" + yqzzTermId;
				}
				Map<byte[], byte[]> yqzzHonors = ActivityGlobalRedis.getInstance().getRedisSession().hGetAllBytes(yqzzHonorKey.getBytes());
				for (byte[] bytes : yqzzHonors.values()) {
					try {
						YQZZWar.PBYQZZLeagueWarServerInfo.Builder serverInfo = YQZZWar.PBYQZZLeagueWarServerInfo.newBuilder();
						serverInfo.mergeFrom(bytes);
						serverInfo.setSeason(yqzzTermId);
						yqzzRankMap.put(serverInfo.getRank(), serverInfo);
					}catch (Exception e){
						HawkException.catchException(e);
					}
				}
				for(int i = 1; i <= 6; i++){
					YQZZWar.PBYQZZLeagueWarServerInfo.Builder serverInfo = yqzzRankMap.get(i);
					if(serverInfo != null){
						builder.addYQZZRank(serverInfo);
					}
				}
				break;
			}
			case SeasonMatchType.S_SW_VALUE: //统帅之战
			{
				int swTermId = timeCfg.getMatchTermId(Activity.SeasonMatchType.S_SW_VALUE);
				Map<Integer, Activity.SeasonSWRankInfo.Builder> swRankMap = new HashMap<>();
				String swHonorKey = "SEASON_HONOR:SW:" + areaId + ":" + swTermId;
				if (!ActivityManager.getInstance().getDataGeter().isSeasonHonorDataNew()) {
					swHonorKey = "SEASON_HONOR:SW:" + swTermId;
				}
				Map<byte[], byte[]> swHonors = ActivityGlobalRedis.getInstance().getRedisSession().hGetAllBytes(swHonorKey.getBytes());
				for (byte[] bytes : swHonors.values()) {
					try {
						Activity.SeasonSWRankInfo.Builder swGuildinfo = Activity.SeasonSWRankInfo.newBuilder();
						swGuildinfo.mergeFrom(bytes);
						swRankMap.put(swGuildinfo.getRank(), swGuildinfo);
					}catch (Exception e){
						HawkException.catchException(e);
					}
				}
				for(int i = 1; i <= 5; i++){
					Activity.SeasonSWRankInfo.Builder swGuildinfo = swRankMap.get(i);
					if(swGuildinfo != null){
						builder.addSWRank(swGuildinfo);
					}
				}
				break;
			}
			case SeasonMatchType.S_XHJZ_VALUE: //星海激战
			{
				int xhjzTermId = timeCfg.getMatchTermId(Activity.SeasonMatchType.S_XHJZ_VALUE);
				Map<Integer, Activity.SeasonXHJZRankInfo.Builder> xhjzRankMap = new HashMap<>();
				String xhjzHonorKey = "SEASON_HONOR:XHJZ:" + areaId + ":" + xhjzTermId;
				Map<byte[], byte[]> xhjzHonors = ActivityGlobalRedis.getInstance().getRedisSession().hGetAllBytes(xhjzHonorKey.getBytes());
				for (byte[] bytes : xhjzHonors.values()) {
					try {
						Activity.SeasonXHJZRankInfo.Builder xhjzGuildinfo = Activity.SeasonXHJZRankInfo.newBuilder();
						xhjzGuildinfo.mergeFrom(bytes);
						xhjzRankMap.put(xhjzGuildinfo.getRank(), xhjzGuildinfo);
					}catch (Exception e){
						HawkException.catchException(e);
					}
				}
				for(int i = 1; i <= 9; i++){
					Activity.SeasonXHJZRankInfo.Builder xhjzGuildinfo = xhjzRankMap.get(i);
					if(xhjzGuildinfo != null){
						builder.addXhjzRank(xhjzGuildinfo);
					}
				}
				break;
			}
			default:
				break;
		} //switch end
	}
	
}

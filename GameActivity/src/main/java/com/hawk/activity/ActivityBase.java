package com.hawk.activity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.hawk.activity.event.impl.CommonActivityLoginEvent;
import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.log.HawkLog;
import org.hawk.msg.HawkMsg;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.obj.HawkObjBase;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.profiler.HawkProfilerAnalyzer;
import org.hawk.task.HawkTaskManager;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.GeneratedMessage;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.constant.ActivityConst;
import com.hawk.activity.constant.ObjType;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.entity.ActivityPlayerEntity;
import com.hawk.activity.entity.IActivityEntity;
import com.hawk.activity.event.MsgArgsCallBack;
import com.hawk.activity.event.MsgCallBack;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.msg.ActivityCallBackMsg;
import com.hawk.activity.msg.PlayerRewardByIdFromActivityMsg;
import com.hawk.activity.msg.PlayerRewardFromActivityMsg;
import com.hawk.activity.timeController.ITimeController;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.SysProtocol.HPOperateSuccess;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 游戏活动抽象
 * 
 * @author PhilChen
 *
 */
public abstract class ActivityBase {

	public final Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 活动id
	 */
	private int activityId;

	private ActivityEntity activityEntity;

	private ActivityDataProxy dataGeter;

	/**
	 * 获取活动类型
	 * 
	 * @return
	 */
	public abstract ActivityType getActivityType();

	public abstract ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity);

	public ActivityBase(int activityId, ActivityEntity activityEntity) {
		this.activityId = activityId;
		this.activityEntity = activityEntity;
	}
	
	public ActivityEntity getActivityEntity() {
		return activityEntity;
	}

	/**
	 * 获取活动实例(全服/个人)
	 * @param playerId
	 * @return
	 */
	public IActivityEntity getIActivityEntity(String playerId){
		IActivityEntity iActivityEntity = getPlayerActivityEntity(playerId);
		if(iActivityEntity == null){
			iActivityEntity = activityEntity;
		}
		return iActivityEntity;
	}

	public ActivityCfg getActivityCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(ActivityCfg.class, activityId);
	}

	public int getActivityId() {
		return activityId;
	}

	public void setDataGeter(ActivityDataProxy dataGeter) {
		this.dataGeter = dataGeter;
	}

	public ActivityDataProxy getDataGeter() {
		return dataGeter;
	}
	
	/**
	 * 获取当前活动期数,返回 0表示未开启
	 * @param playerId
	 * @return
	 */
	public int getActivityTermId(String playerId) {
		IActivityEntity iActivityEntity = getIActivityEntity(playerId);
		if (iActivityEntity.getActivityState() == ActivityState.HIDDEN) {
			return 0;
		}
		return iActivityEntity.getTermId();
	}
	/**
	 * 获取当前活动期数(仅限不是根据玩家单独开启的活动调用)
	 * @return
	 */
	public int getActivityTermId() {
		if (getTimeControl().isPlayerActivityTimeController()) {
			throw new RuntimeException("wrong use for PlayerActivity");
		}
		ActivityEntity activityEntity = getActivityEntity();
		if (activityEntity.getActivityState() == ActivityState.HIDDEN) {
			return 0;
		}
		return activityEntity.getTermId();
	}
	
	/**
	 * 获取活动的时间控制器
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends ITimeController> T getTimeControl() {
		return (T)getActivityType().getTimeControl();
	}
	
	/**
	 * 同一玩家调用此方法需要保证是线程安全的，即每次调用都是在同一线程中，即玩家所在线程
	 * 
	 * @param playerId
	 * @return
	 */
	public <T extends HawkDBEntity> Optional<T> getPlayerDataEntity(String playerId) {
		return getPlayerDataEntity(playerId, true);
	}
	
	/**
	 * 同一玩家调用此方法需要保证是线程安全的，即每次调用都是在同一线程中，即玩家所在线程
	 * 
	 * @param playerId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends HawkDBEntity> Optional<T> getPlayerDataEntity(String playerId, boolean autoCreate) {
		ActivityCfg cfg = getActivityCfg();
		if (cfg == null) {
			return Optional.empty();
		}

		// 活动未开启
		int termId = getActivityTermId(playerId);		
		if (termId == 0) {
			return Optional.empty();
		}
		
		// 缓存获取
		HawkDBEntity entity = PlayerDataHelper.getInstance().getActivityDataEntity(playerId, getActivityType());
		if (entity != null) {
			IActivityDataEntity dataEntity = (IActivityDataEntity) entity;
			if (dataEntity.getTermId() == termId) {
				return Optional.of((T) dataEntity);
			}
		}
		
		// 跨服玩家,且该活动跨服开关为关闭,返回空数据
		if (getDataGeter().isCrossPlayer(playerId) && !cfg.isCrossOpen()) {
			return Optional.empty();
		}

		// 数据库获取
		long showTime = getTimeControl().getShowTimeByTermId(termId);
		long startTime = HawkTime.getMillisecond();
		if (startTime - showTime > 300000L) {
			entity = loadFromDB(playerId, termId);
			long costtime = HawkTime.getMillisecond() - startTime;
			if (costtime > 50) {
				HawkProfilerAnalyzer.getInstance().addMsgHandleInfo("activityLoadDB-" + getActivityId(), costtime);
				HawkLog.logPrintln("activity base getPlayerDataEntity load from db, playerId: {}, activityId: {}, termId: {}, costtime: {}", playerId, getActivityId(), termId, costtime);
			}
			if (entity != null) {
				entity = PlayerDataHelper.getInstance().putActivityDataEntity(playerId, getActivityType(), entity);
				return Optional.of((T) entity);
			}
		}
		
		
		// 自动创建新对象
		if (autoCreate) {
			HawkObjBase<HawkXID, HawkAppObj> objBase = HawkApp.getInstance().getObjMan(1).queryObject(HawkXID.valueOf(1, playerId));
			if (objBase != null && objBase.isObjValid()) {
				HawkAppObj appObj = objBase.getImpl();
				synchronized (appObj) {
					entity = createActivityDataEntity(appObj, playerId, termId);
				}
			} else {
				entity = createActivityDataEntity(null, playerId, termId);
			}
		}
		
		return Optional.ofNullable((T) entity);
	}
	
	/**
	 * 创建玩家活动实体数据
	 * @param playerId
	 * @param termId
	 * @return
	 */	
	private HawkDBEntity createActivityDataEntity(HawkAppObj appObj, String playerId, int termId){
		ActivityCfg cfg = getActivityCfg();
		if(cfg == null){
			return null;
		}
		
		// 跨服玩家,且该活动跨服开关为关闭,返回空数据
		if(getDataGeter().isCrossPlayer(playerId) && !cfg.isCrossOpen()){
			return null;
		}
		
		HawkDBEntity entity = createDataEntity(playerId, termId);
		if (entity != null) {
			// 避免出错
			if (entity.getUpdateTime() > 0) {
				throw new RuntimeException("activity data entity create state error");
			}
			
			// 创建并添加到内存
			if (appObj != null) {
				synchronized (appObj) {
					if (entity.create(true)) {
						PlayerDataHelper.getInstance().putActivityDataEntity(playerId, getActivityType(), entity);
					}
				}	
			} else {
				if (entity.create(true)) {
					PlayerDataHelper.getInstance().putActivityDataEntity(playerId, getActivityType(), entity);
				}
			}
		}
		
		return entity;
		
	}
	
	/**
	 * 刷新玩家活动数据缓存生存周期
	 * 
	 * @param playerId
	 */
	public void refreshDataCache(String playerId) {
		ActivityType activityType = getActivityType();
		HawkDBEntity entity = PlayerDataHelper.getInstance().getActivityDataEntity(playerId, activityType);
		if (entity != null) {
			PlayerDataHelper.getInstance().putActivityDataEntity(playerId, activityType, entity);
		}
	}

	/**
	 * 从数据库加载玩家活动数据
	 * @param playerId
	 * @param termId
	 * @return
	 */
	protected abstract HawkDBEntity loadFromDB(String playerId, int termId);
	
	/**
	 * 
	 * @param playerId
	 * @param termId
	 * @return
	 */
	protected abstract HawkDBEntity createDataEntity(String playerId, int termId);

	/**
	 * 同步活动状态信息
	 * @param playerId
	 */
	public void syncActivityStateInfo(String playerId){
		PlayerPushHelper.getInstance().syncActivityStateInfo(playerId, this);
	}

	/**
	 * 同步活动内容数据
	 * @param playerId
	 */
	public void syncActivityDataInfo(String playerId){
	}
	
	/**
	 * 玩家活动数据清除(涉及排行信息的,如果活动开启,则需要具体实现清除排行数据)
	 * @param playerId
	 * @return
	 */
	public void onPlayerMigrate(String playerId) {
		
	}
	
	/**
	 * 玩家数据迁入处理(排行激活)
	 * @param playerId
	 */
	public void onImmigrateInPlayer(String playerId) {
		
	}
	
	/**
	 * 移除排行榜数据
	 * @param playerId
	 */
	public void removePlayerRank(String playerId) {
	}

	/**
	 * 获取玩家活动期数数据(注册开启类活动)
	 * @param playerId
	 * @return
	 */
	public ActivityPlayerEntity getPlayerActivityEntity(String playerId) {
		if(getTimeControl().isPlayerActivityTimeController()){
			ActivityPlayerEntity entity = PlayerDataHelper.getInstance().getPlayerActivityEntity(playerId, this.getActivityId());
			return entity;
		}
		return null;
	}
	
	/**
	 * 为新玩家创建根据注册时间开启的活动数据对象, 内存创建, 不在此进行db的create
	 * @param playerId
	 * @return
	 */
	public ActivityPlayerEntity preparePlayerActivityEntity(String playerId) {
		Map<Integer, ActivityPlayerEntity> playerActivityEntityMap = PlayerDataHelper.getInstance().getPlayerData(playerId).getPlayerActivityEntityMap();
		if(!playerActivityEntityMap.containsKey(this.activityId)){
			ActivityPlayerEntity entity = new ActivityPlayerEntity();
			entity.setPlayerId(playerId);
			entity.setActivityId(this.activityId);
			entity.setTermId(0);
			entity.setState(ActivityState.HIDDEN.intValue());
			playerActivityEntityMap.put(this.activityId, entity);
			return entity;
		}
		return null;
	}
	
	/**
	 * 玩家所在大区&渠道是否可开启该活动
	 * @param playerId
	 * @return
	 */
	public boolean isChannelAllow(String playerId) {
		List<String> channelList = getActivityCfg().getChannelList();
		List<String> areaList = getActivityCfg().getAreaList();
		String channelId = getDataGeter().getPlayerChannelId(playerId);
		String channel = getDataGeter().getPlayerChannel(playerId);
		return (channelList.isEmpty() || channelList.contains(channelId)) && (areaList.isEmpty() || areaList.contains(channel));
	}
	
	/**
	 * 玩家当前客户端版本号是否可开启该活动
	 * @param playerId
	 * @return
	 */
	public boolean isVersionAllow(String playerId){
		List<Integer> limitList = getActivityCfg().getVersionLimitInfo();
		// 无版本号限制
		if(limitList.isEmpty()){
			return true;
		}
		String version = getDataGeter().getPlayerVersion(playerId);
		String[] infoArr = version.split("\\.");
		// 版本号格式不一致
		if(infoArr.length - 1 != limitList.size()){
			return false;
		}
		try {
			for(int i = 0; i< limitList.size();i++){
				int info = Integer.valueOf(infoArr[i]);
				if(info < limitList.get(i)){
					return false;
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	/**
	 * 自定义改变活动关闭状态
	 * @param playerId
	 * @return
	 */
	public boolean isActivityClose(String playerId) {
		return false;
	}

	/**
	 * 是否已通过GM指令关闭
	 * @return
	 */
	public boolean isGmClose(){
		return getDataGeter().isGmClose(getActivityType());
	}

	/**
	 * 活动是否失效
	 * @return
	 */
	public boolean isInvalid() {
		// 判定是否通过GM指令关闭该活动
		if (isGmClose()) {
			//logger.info("activity activityId: {} by gmserver limit open", activityId);
			return true;
		}
		return ActivityManager.getInstance().isActivityInvalid(getActivityCfg());
	}

	public boolean isShow(){
		// 活动是否失效
		if (isInvalid()) {
			return false;
		}
		ActivityState state = getIActivityEntity("").getActivityState();
		if (state == ActivityState.HIDDEN) {
			return false;
		}
		return true;
	}

	/**
	 * 活动是否展示(非隐藏阶段)
	 * @param playerId
	 * @return
	 */
	public boolean isShow(String playerId) {
		// 活动是否失效
		if (isInvalid()) {
			return false;
		}

		// 玩家渠道是否许可开启此活动
		if (!isChannelAllow(playerId)) {
			return false;
		}

		// 玩家客户端版本是否满足限制条件
		if (!isVersionAllow(playerId)) {
			return false;
		}
		ActivityState state = getIActivityEntity(playerId).getActivityState();
		if (state == ActivityState.HIDDEN) {
			return false;
		}

		// 玩家是否已达成活动关闭条件
		if (isActivityClose(playerId)) {
			return false;
		}
		return true;
	}

	/**
	 * 活动是否在展示阶段
	 * @param playerId
	 * @return
	 */
	public boolean isShowing(String playerId) {
		// 活动是否失效
		if (isInvalid()) {
			return false;
		}

		// 玩家渠道是否许可开启此活动
		if (!isChannelAllow(playerId)) {
			return false;
		}

		// 玩家客户端版本是否满足限制条件
		if (!isVersionAllow(playerId)) {
			return false;
		}

		ActivityState state = getIActivityEntity(playerId).getActivityState();
		if (state != ActivityState.SHOW) {
			return false;
		}

		// 玩家是否已达成活动关闭条件
		if (isActivityClose(playerId)) {
			return false;
		}
		return true;
	}
	
	/**
	 * 活动是否开启（活动未被屏蔽,且处于开启阶段) 
	 * @return
	 */
	public boolean isOpening(String playerId) {
		// 活动是否失效
		if (isInvalid()) {
			return false;
		}
		ActivityState state = getIActivityEntity(playerId).getActivityState();
		if (state != ActivityState.OPEN) {
			return false;
		}
		if (isActivityClose(playerId)) {
			return false;
		}
		return true;
	}

	/**
	 * 活动是否隐藏
	 * 
	 * @return
	 */
	public boolean isHidden(String playerId) {
		return !isShow(playerId);
	}

	/**
	 * 活动帧更新
	 */
	public void onTick() {
	}

	/**
	 * 快速tick 200ms
	 */
	public void onQuickTick() {
		
	}
	
	public void onPlayerLogin(String playerId) {
	}

	public void onPlayerLogout(String playerId) {
	}

	/**
	 * 玩家线程tick 不到万不得已不要使用
	 * @param playerId
	 */
	public void onPlayerTick(String playerId) {

	}

	public void onShow() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		int msgId = MsgId.SYNC_ACTIVITY_DATA_INFO * ActivityConst.MSGID_OFFSET + getActivityId();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, msgId, () -> {
				if (isShow(playerId)) {
					syncActivityDataInfo(playerId);
				}
			});
		}
	}
	
	/**
	 * 单个玩家的活动展示事件(每个人家的活动时间可能存在不一样)
	 * 
	 * @param playerId
	 */
	public void onShowForPlayer(String playerId) {
	}
	
	
	/**
	 * 合服埋了一个坑在这里,
	 * 合服的时候会删除活动的总表,导致合服之后再开的活动会再走一遍onOpen.
	 * 有疑问清 {@author jm}
	 */
	public void onOpen() {
		
	}

	/**
	 * 单个玩家的活动开始事件(每个人家的活动时间可能存在不一样)
	 * 
	 * @param playerId
	 */
	public void onOpenForPlayer(String playerId) {
		
	}

	/**
	 * 状态时间点相同的活动结束事件
	 */
	public void onEnd() {
		//TODO 结束时发放未完成的奖励
	}
	
	/**
	 * 单个玩家的活动结束事件(每个人家的活动时间可能存在不一样)
	 * 
	 * @param playerId
	 */
	public void onEndForPlayer(String playerId) {
	}

	/**
	 * 状态时间点相同的活动隐藏事件
	 */
	public void onHidden() {
	}

	/**
	 * 单个玩家的活动隐藏事件(每个人家的活动时间可能存在不一样)
	 * 
	 * @param playerId
	 */
	public void onHiddenForPlayer(String playerId) {
	}
	
	/**
	 * GM关闭活动
	 */
	public void onGmCloseActivity(){
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			syncActivityStateInfo(playerId);
		}
	}
	
	/**
	 * GM开启期活动
	 */
	public void onGmOpenActivity(){
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			syncActivityStateInfo(playerId);
			if(isShow(playerId)){
				syncActivityDataInfo(playerId);
			}
		}
	}
	
	/**
	 * 检测活动结束状态,若已结束,则提前关闭活动
	 * @param playerId
	 */
	public void checkActivityClose(String playerId){
		if(isActivityClose(playerId)){
			syncActivityStateInfo(playerId);
		}
	}

	/**
	 * 是否允许进行活动相关协议请求操作
	 * 
	 * @return
	 */
	public boolean isAllowOprate(String playerId) {
		return isShow(playerId);
	}
	
	@SuppressWarnings("rawtypes")
	protected void pushToPlayer(String playerId, int type, GeneratedMessage.Builder builder) {
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(type, builder));
	}
	
	/**
	 * 返回错误报告
	 * @param playerId
	 * @param hpCode
	 * @param errCode
	 */
	@Deprecated
	public void sendError(String playerId, int hpCode, int errCode){
		PlayerPushHelper.getInstance().sendError(playerId, hpCode, errCode);
	}
	
	/**
	 * 发送错误码,并且抛出异常。
	 * @param playerId
	 * @param hpCode
	 * @param errCode
	 */
	public void sendErrorAndBreak(String playerId, int hpCode, int errCode) {
		PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hpCode, errCode);
	}
	
	/**
	 * 如果是check为true 则发错误码并且抛出异常.
	 * 条件校验大多都是针对异常条件判真,然后中断.
	 * @param playerId
	 * @param hpCode
	 * @param errCode
	 */
	public void checkTrueAndBreak(boolean check, String playerId, int hpCode, int errCode) {
		if (check) {
			sendErrorAndBreak(playerId, hpCode, errCode);
		}
	}
	/**
	 * 向其他模块发送消息
	 * 
	 * @param msgId
	 * @param playerId
	 * @param args
	 */
	public void postMsg(String playerId, HawkMsg msg) {
		getDataGeter().isInDungeonState(playerId); // 强制makeSurePlayer 如果玩家对象不在内存会导致post失败 .  dispatch msg lock object miss, xid: (1, fgg-hqfyq-i1)
		HawkXID xid = HawkXID.valueOf(ObjType.PLAYER, playerId);
		HawkTaskManager.getInstance().postMsg(xid, msg);
	}

	/**
	 * 根据奖励对象给玩家添加奖励
	 * 
	 * @param playerId
	 * @param reward
	 */
	public void postReward(String playerId, ActivityReward reward) {
		postReward(playerId, reward, true);
	}
	
	/**
	 * 根据奖励对象给玩家添加奖励
	 * 
	 * @param playerId
	 * @param reward
	 * @param rewardMerge 奖励是否需要合并
	 */
	public void postReward(String playerId, ActivityReward reward, boolean rewardMerge) {
		PlayerRewardFromActivityMsg msg = PlayerRewardFromActivityMsg.valueOf(reward.getRewardList(), reward.getBehaviorAction(),
				reward.isAlert(), reward.getOrginType(), reward.getActivityId());
		msg.setMerge(rewardMerge);
		msg.setAwardReason(reward.getAwardReason());
		postMsg(playerId, msg);
	}

	/**
	 * 根据奖励id给玩家添加奖励
	 * 
	 * @param playerId
	 * @param rewardId
	 * @param action
	 */
	protected void postRewardById(String playerId, int rewardId, Action action, MsgArgsCallBack callBack) {
		PlayerRewardByIdFromActivityMsg msg = PlayerRewardByIdFromActivityMsg.valueOf(rewardId, action, callBack);
		postMsg(playerId, msg);
	}
	
	/**
	 * <pre>
	 * 抛去玩家线程执行的回调，避免多线程问题
	 * 活动自有的所有on事件（onShow、onOpen、onEnd、onHidden）与palyer业务逻辑处在不同的线程
	 * 凡是活动on事件中需要调用getPlayerDataEntity方法的请将逻辑通过回调方式编写
	 * </pre>
	 * 
	 * @param msgId
	 * @param playerId
	 * @param callBack
	 */
	public void callBack(String playerId, int msgId, MsgCallBack callBack) {
		ActivityCallBackMsg msg = ActivityCallBackMsg.valueOf(callBack, msgId);
		postMsg(playerId, msg);
	}
	
	/**
	 * 给玩家发送邮件
	 * @param playerId
	 * @param mailId
	 * @param title
	 * @param subTitle
	 * @param content
	 * @param items
	 * @param isGetReward
	 */
	public void sendMailToPlayer(String playerId, MailId mailId, Object[] title, Object[] subTitle, Object[] content, List<RewardItem.Builder> items, boolean isGetReward) {
		ActivityManager.getInstance().getDataGeter().sendMail(playerId, mailId, title, subTitle, content, items, isGetReward);
		logger.info("activity send mail, playerId: {}, mailId: {}, isGetReward: {}", playerId, mailId, isGetReward);
	}
	
	public void sendMailToPlayer(String playerId, MailId mailId, Object[] subTitle, Object[] content, List<RewardItem.Builder> items) {
		sendMailToPlayer(playerId, mailId, new Object[]{}, subTitle, content, items);
	}
	
	public void sendMailToPlayer(String playerId, MailId mailId, Object[] title,Object[] subTitle, Object[] content, List<RewardItem.Builder> items) {
		sendMailToPlayer(playerId, mailId, title, subTitle, content, items, false);
	}
	
	public void addWorldBroadcastMsg(ChatType chatType, Const.NoticeCfgId key, String playerId, Object... parms) {
		ActivityManager.getInstance().getDataGeter().addWorldBroadcastMsg(chatType, key, playerId, parms);
	}
	
	public <T extends AchieveConfig> String getDefaultRewards(int achieveId, Class<T> clazz){
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(clazz, achieveId);
		if(config == null){
			return null;
		}
		return config.getReward();
	}
	
	public void sendBroadcast(Const.NoticeCfgId key, String playerId, Object... parms) {
		ActivityManager.getInstance().getDataGeter().sendBroadcat(key, playerId, parms);
	}
	
	/**
	 * 外层统一记录是否调用,通过该返回值来决定是否二次调用(防止脚本多次执行).
	 * @return 
	 */
	public boolean handleForMergeServer() {
		return false;
	}
	
	public void shutdown() {
		
	}
	
	/**
	 * 通用的操作成功回复协议
	 */
	public void responseSuccess(String playerId, int hpCode) {
		HPOperateSuccess.Builder builder = HPOperateSuccess.newBuilder().setHpCode(hpCode);
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.sys.OPERATE_SUCCESS, builder));
	}

	public void postCommonLoginEvent(String playerId){
		ActivityManager.getInstance().postEvent(new CommonActivityLoginEvent(playerId, getActivityId()));
	}
}

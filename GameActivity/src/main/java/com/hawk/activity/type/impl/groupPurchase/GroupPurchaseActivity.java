package com.hawk.activity.type.impl.groupPurchase;

import java.util.*;

import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Activity.ScoreState;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.constant.ActivityConst;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.BuyFundEvent;
import com.hawk.activity.event.impl.GroupPurchaseEvent;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.groupPurchase.cfg.GroupPurchaseAchieveCfg;
import com.hawk.activity.type.impl.groupPurchase.cfg.GroupPurchaseActivityKVCfg;
import com.hawk.activity.type.impl.groupPurchase.entity.GroupPurchaseEntity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.game.protocol.Activity.GroupPurcharseInfoSync;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 跨服团购活动
 * @author Jesse
 *
 */
public class GroupPurchaseActivity extends ActivityBase implements AchieveProvider {
	/**
	 * 全服积分
	 */
	private int globalScore = 0;
	private int globalScoreOld = 0;
	
	/**
	 * 上次自动添加积分时间
	 */
	private long lastAddScoreTime = 0;
	
	public GroupPurchaseActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public boolean isProviderActive(String playerId) {
		return isOpening(playerId);
	}
	
	@Override
	public boolean isProviderNeedSync(String playerId) {
		return !isHidden(playerId);
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	/**
	 * 是否允许进行活动相关协议请求操作
	 * 
	 * @return
	 */
	@Override
	public boolean isAllowOprate(String playerId) {
		return isShow(playerId);
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		if (isOpening(playerId)) {
			ActivityManager.getInstance().postEvent(new GroupPurchaseEvent(playerId));
			this.syncActivityDataInfo(playerId);
		}
	}

	/**
	 * 玩家登陆事件的监听函数
	 *
	 * @param event
	 */
	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		//活动是否开启，没开不继续处理
		if (!isOpening(playerId)) {
			return;
		}
		//去当前期数
		int termId = getActivityTermId(playerId);
		//取当前期数结束时间
		long endTime = getTimeControl().getEndTimeByTermId(termId, playerId);
		//取当前时间
		long now = HawkTime.getMillisecond();
		//如果当前时间大于当前期数结束时间，不继续处理
		if (now >= endTime) {
			return;
		}
		Optional<GroupPurchaseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}

		GroupPurchaseEntity entity = opEntity.get();
		int loginDay = HawkTime.getYyyyMMddIntVal();
		//玩家数据里记录当前天的的日期，说明已经处理过每日登陆成就，不继续处理
		if (entity.getLoginDay() == loginDay) {
			return;
		}
		//记录当天日期
		entity.setLoginDay(loginDay);
		//清除每日宝箱领取记录
		entity.setDailyRewardReceive(false);
		//给客户端同步下可领取状态
		syncActivityDataInfo(playerId);
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_GROUP_PURCHASE, ()-> {
				initAchieveInfo(playerId);
			});
		}
	}
	
	/**
	 * 同步积分
	 * @param playerId
	 */
	public void syncGlobalScore(String playerId, boolean isDailyRewardReceive) {
		Optional<GroupPurchaseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		GroupPurchaseEntity entity = opEntity.get();
		Set<String> stateSet = entity.getScoreState();

		GroupPurcharseInfoSync.Builder builder = GroupPurcharseInfoSync.newBuilder();
		builder.setScore(getScore());
		builder.setScoreOld(this.globalScoreOld);
		builder.setIsDailyReceive(isDailyRewardReceive);
		for(String str : stateSet){
			ScoreState.Builder stateBuilder = ScoreState.newBuilder();
			String[] strAry = str.split(SerializeHelper.ATTRIBUTE_SPLIT);
			if(strAry.length != 2){
				HawkLog.errPrintln("GroupPurchaseActivity syncGlobalScore error, playerId: {}, ScoreState str: {}", playerId, str);
				continue;
			}
			stateBuilder.setActivityType(Integer.valueOf(strAry[0]));
			stateBuilder.setSubType(Integer.valueOf(strAry[1]));
			builder.addScoreState(stateBuilder);
		}
		pushToPlayer(playerId, HP.code.GROUP_PURCHASE_SCORE_SYNC_S_VALUE, builder);
	}

	public void onDailyRewardReceiveReq(String playerId){
		Optional<GroupPurchaseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		GroupPurchaseEntity entity = opEntity.get();
		//已经领取，不能重复领取
		if(entity.isDailyRewardReceive()){
			return;
		}

		GroupPurchaseActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GroupPurchaseActivityKVCfg.class);
		if(cfg == null){
			return;
		}
		//记录每日登陆礼包已领取
		entity.setDailyRewardReceive(true);
		//发礼包奖励
		this.getDataGeter().takeReward(playerId,
				RewardHelper.toRewardItemImmutableList(cfg.getDailyReward()),
				1, Action.ACTIVITY_GROUP_PURCHASE_DAILY_AWARD, true);

		//给客户端同步活动状态
		this.syncActivityDataInfo(playerId);
	}
	
	@Override
	public void onTick() {
		int termId = getActivityEntity().getTermId();
		int addScore = 0;
		long now = HawkTime.getMillisecond();
		GroupPurchaseActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GroupPurchaseActivityKVCfg.class);
		if(cfg == null){
			return;
		}
		int addScoreType = cfg.getAddScoreType();
		long startTimeLimit = 0;
		long endTimeLimit = 0;
		switch (addScoreType) {
		case ActivityConst.GROUP_PURCHASE_SCORE_TIME_TYPE1:
			long serverOpenTime = ActivityManager.getInstance().getDataGeter().getServerOpenDate();
			startTimeLimit = cfg.getAddStartTimeValue1() * 1000 + serverOpenTime;
			endTimeLimit = cfg.getAddEndTimeValue1() * 1000 + serverOpenTime;
			break;
		case ActivityConst.GROUP_PURCHASE_SCORE_TIME_TYPE2:
			startTimeLimit = cfg.getAddStartTimeValue2();
			endTimeLimit = cfg.getAddEndTimeValue2();
			break;

		default:
			return;
		}

		// 不在可自动增加积分的时间段内,则不进行积分增加
		if( startTimeLimit > now || now > endTimeLimit){
			return;
		}

		// 若上次积分增长时间在限制开启时间之前,则刷新为当前时间
		if (lastAddScoreTime <= startTimeLimit) {
			lastAddScoreTime = now;
		} else {
			addScore = autoAddScore(now);
		}
		
		String scoreKey = ActivityRedisKey.GROUP_PURCHASE_SCORE + termId;
		int newScore = 0;
		if(termId != 0){
			newScore = getResidScore(scoreKey);
		}
		
		if (addScore > 0) {
			newScore += addScore;
			ActivityLocalRedis.getInstance().set(scoreKey, String.valueOf(newScore));
		}
		int oldScore = this.globalScore;
		if (this.globalScore != newScore) {
			this.globalScoreOld = this.globalScore;
			this.globalScore = newScore;
			checkScoreAchieve(oldScore, newScore);
		}
	}
	
	/**
	 *  起服后随时间自动增加积分
	 * @param now
	 * @return
	 */
	private int autoAddScore(long now) {
		long oldTime = lastAddScoreTime;
		long timeGap = now - oldTime;
		GroupPurchaseActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GroupPurchaseActivityKVCfg.class);
		if (cfg == null) {
			return 0;
		}
		long gap = cfg.getAddScoreTimeGap();
		int baseScore = cfg.getAddScore();
		if (gap == 0 || baseScore == 0) {
			return 0;
		}
		int gapCnt = (int) (timeGap / gap);
		if(gapCnt == 0){
			return 0;
		}
		this.lastAddScoreTime += gapCnt * gap;
		int addScore = gapCnt * baseScore;
		super.logger.info("GroupPurchaseActivity auto add score. lastAddTime: {}, currTime: {}, gapCnt: {}, addScore: {}",
				oldTime, now, gapCnt, addScore);
		return addScore;
	}

	/**
	 * 全服积分达标检测
	 * @param oldScore
	 * @param newScore
	 */
	private boolean checkScoreAchieve(int oldScore, int newScore) {
		 ConfigIterator<GroupPurchaseAchieveCfg> its = HawkConfigManager.getInstance().getConfigIterator(GroupPurchaseAchieveCfg.class);
		boolean needPush = false;
		 for(GroupPurchaseAchieveCfg cfg : its){
			int needScore =  cfg.getConditionValue(0);
			if(oldScore<needScore && newScore>=needScore){
				needPush = true;
				break;
			}
		}
		 if(needPush){
			Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
			for (String playerId : onlinePlayerIds) {
				ActivityManager.getInstance().postEvent(new GroupPurchaseEvent(playerId));
			}
		 }
		 return needPush;
	}

	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<GroupPurchaseEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		GroupPurchaseEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		ConfigIterator<GroupPurchaseAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(GroupPurchaseAchieveCfg.class);
		while (configIterator.hasNext()) {
			GroupPurchaseAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}
		
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}
	
	@Subscribe
	public void onEvent(BuyFundEvent event) {
		String playerId = event.getPlayerId();
		int termId = getIActivityEntity(playerId).getTermId();
		String scoreKey = ActivityRedisKey.GROUP_PURCHASE_SCORE + termId;
		int score = 0;
		if (termId != 0) {
			score = getResidScore(scoreKey);
		}

		int newScore = score + 1;
		ActivityLocalRedis.getInstance().set(scoreKey, String.valueOf(newScore));

		int oldScore = this.globalScore;
		this.globalScore = newScore;
		this.globalScoreOld = oldScore;
		boolean needPush = checkScoreAchieve(oldScore, newScore);
		if (!needPush) {
			ActivityManager.getInstance().postEvent(new GroupPurchaseEvent(playerId));
		}
		Optional<GroupPurchaseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		GroupPurchaseEntity entity = opEntity.get();
		entity.setScoreState(event.getActiveType(), event.getSubType());
		this.syncActivityDataInfo(playerId);
	}
	/**
	 * 获取globalredis中记录的积分
	 * @return
	 */
	public int getResidScore(String scoreKey){
		String scoreStr = ActivityLocalRedis.getInstance().get(scoreKey);
		if (!HawkOSOperator.isEmptyString(scoreStr)) {
			return Integer.parseInt(scoreStr);
		}
		return 0;
	}
	
	/**
	 * 获取全服团购积分
	 * @return
	 */
	public int getScore(){
		return this.globalScore;
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<GroupPurchaseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		GroupPurchaseEntity entity = opEntity.get();
		if(entity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public GroupPurchaseAchieveCfg getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(GroupPurchaseAchieveCfg.class, achieveId);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.GROUP_PURCHASE_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.ACTIVITY_GROUP_PURCHASE_AWARD;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		GroupPurchaseActivity activity = new GroupPurchaseActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<GroupPurchaseEntity> queryList = HawkDBManager.getInstance()
				.query("from GroupPurchaseEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			GroupPurchaseEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		GroupPurchaseEntity entity = new GroupPurchaseEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		syncGlobalScore(playerId, this.isDailyRewardReceive(playerId));
	}
	
	@Override
	public boolean isActivityClose(String playerId) {
		Optional<GroupPurchaseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		GroupPurchaseEntity entity = opEntity.get();
		List<AchieveItem> itemList = entity.getItemList();
		if (itemList == null || itemList.isEmpty()) {
			return false;
		}

		for (AchieveItem item : entity.getItemList()) {
			if (item.getState() != AchieveState.TOOK_VALUE) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}

	private boolean isDailyRewardReceive(String playerId){
		Optional<GroupPurchaseEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return true;
		}
		GroupPurchaseEntity entity = opEntity.get();
		return entity.isDailyRewardReceive();
	}

	@Override
	public void onTakeRewardSuccess(String playerId) {
		checkActivityClose(playerId);
	}

	@Override
	public void onPlayerMigrate(String playerId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		// TODO Auto-generated method stub
		
	}

}

package com.hawk.activity.type.impl.timeLimitLogin;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.timeLimitLogin.cfg.TimeLimitLoginAwardCfg;
import com.hawk.activity.type.impl.timeLimitLogin.cfg.TimeLimitLoginKVCfg;
import com.hawk.activity.type.impl.timeLimitLogin.entity.TimeLimitLoginEntity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.LimitLoginInfo;
import com.hawk.game.protocol.Activity.LimitLoginStatus;
import com.hawk.game.protocol.Activity.LimitTimeLoginInfo;
import com.hawk.game.protocol.Common.KeyValuePairStr;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.serialize.string.SerializeHelper;

public class TimeLimitLoginActivity extends ActivityBase {
	//当前活动状态
	public static final int BEFORE_STATUS = 1;//之前
	public static final int IN_STATUS = 2; //期间
	//奖励领取状态
	public static final int NO_RECEIVE = 1;    //未领
	public static final int HAVE_RECEIVED = 2; //已领
	
	public TimeLimitLoginActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.TIME_LIMIT_LOGIN_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		TimeLimitLoginActivity activity = new TimeLimitLoginActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<TimeLimitLoginEntity> queryList = HawkDBManager.getInstance()
				.query("from TimeLimitLoginEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			return queryList.get(0);
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		TimeLimitLoginEntity entity = new TimeLimitLoginEntity(playerId, termId);
		return entity;
	}
	

	@Override
	public void syncActivityDataInfo(String playerId) {
		syncLimitLoginInfo(playerId);
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.LIMIT_TIME_LOGIN_SYNC, ()-> {
				syncLimitLoginInfo(playerId);
			});
		}
	}

	@Override
	public void onTick() {
	}
	
	/**同步活动信息
	 * @param playerId
	 * @return
	 */
	public Result<Void> timeLimitLoginInfo(String playerId){
		Optional<TimeLimitLoginEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		//同步信息
		syncActivityDataInfo(playerId);
		return Result.success();
		
	}
	/** 限时登录领取奖励
	 * @param playerId
	 * @param id
	 */
	public Result<Void> receiveReward(String playerId, int id){
		Optional<TimeLimitLoginEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		LimitLoginInfo.Builder limitBuilder = getCurrentInfoByTime(playerId);
		if (limitBuilder == null) {
			return Result.fail(Status.Error.TIME_LIMIT_ROUND_END_VALUE);
		}
		if (id != limitBuilder.getTermId()) {
			return Result.fail(Status.Error.TIME_LIMIT_ROUND_NO_SAME_VALUE);
		}
		if (limitBuilder.getStatus() != IN_STATUS) {
			return Result.fail(Status.Error.TIME_LIMIT_NOT_IN_REWARD_STATE_VALUE);
		}
		TimeLimitLoginAwardCfg awardCfg = HawkConfigManager.getInstance().getConfigByKey(TimeLimitLoginAwardCfg.class, id);
		//发奖
		this.getDataGeter().takeReward(playerId, awardCfg.getRewardList(), 1, Action.TIME_LIMIT_LOGIN_REWARD, true, RewardOrginType.TIME_LIMIT_LOGIN_REWARD);
		//打点
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), id);
		//更新数据
		TimeLimitLoginEntity entity = opEntity.get();
		entity.addStatus(id);
		//同步活动数据
		syncLimitLoginInfo(playerId);
		return Result.success();
	}
	/**同步玩家活动数据
	 * @param playerId
	 */
	public void syncLimitLoginInfo(String playerId){
		LimitTimeLoginInfo.Builder builder = LimitTimeLoginInfo.newBuilder();
		LimitLoginInfo.Builder limitBuilder = getCurrentInfoByTime(playerId);
		if (limitBuilder != null) {
			builder.setLimitInfo(limitBuilder);
		}
		int termId = limitBuilder != null ? limitBuilder.getTermId() : 0;
		List<LimitLoginStatus> statusBuilder = getLimitStatus(playerId, termId);
		if (statusBuilder == null) {
			return;
		}
		builder.addAllLimitStatus(statusBuilder);
		//同步信息
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.TIME_LIMIT_LOGIN_INFO_SYN_VALUE, builder));
	}
	/**
	 * 根据当前时间获取活动当前阶段Id
	 */
	public LimitLoginInfo.Builder getCurrentInfoByTime(String playerId){
		Optional<TimeLimitLoginEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return null;
		}
		TimeLimitLoginEntity entity = opEntity.get();
		long now  = HawkTime.getMillisecond();
		//获取活动配置的起始时间
		int actTermId = getActivityTermId();
		//活动开始时间的当天的0点
		long startTimeAct = HawkTime.getAM0Date(new Date(getTimeControl().getStartTimeByTermId(actTermId))).getTime();
		//活动结束时间
		long endTimeAct = getTimeControl().getEndTimeByTermId(actTermId);
		if (now > endTimeAct) {
			return null;
		}
		LimitLoginInfo.Builder liBuilder =  LimitLoginInfo.newBuilder();
		ConfigIterator<TimeLimitLoginAwardCfg> aConfigIterator = HawkConfigManager.getInstance().getConfigIterator(TimeLimitLoginAwardCfg.class);
		//遍历每一条
		while (aConfigIterator.hasNext()) {
			TimeLimitLoginAwardCfg awardCfg = aConfigIterator.next();
			int id = awardCfg.getId();
			//生效天有两个时间点   11:00-13:00 //  17:00-19:00
			KeyValuePairStr.Builder builder = getTimeInfo(id, startTimeAct);
			if (builder != null) {
				//在期间内，但是已领取奖励，跳到下一环
				if (builder.getKey() == IN_STATUS && entity.containStatus(id)) {
					int termId = id + 1;
					//超出size , 则返回null 说明最后一轮结束
					if (termId > aConfigIterator.size()) {
						return null;
					}
					TimeLimitLoginAwardCfg tAwardCfg = HawkConfigManager.getInstance().getConfigByKey(TimeLimitLoginAwardCfg.class, termId);
					//不在天数内 ,则返回null 说明最后一轮结束
					if (tAwardCfg.getDay() > getDifDaysStartToEnd()) {
						return null;
					}
					liBuilder.setTermId(termId);
					liBuilder.setStatus(BEFORE_STATUS);
					long downTime = getDownTimeByIdAndType(termId, startTimeAct, true);
					liBuilder.setDownTime(downTime);
				}else{
					liBuilder.setTermId(id);
					liBuilder.setStatus(builder.getKey());
					liBuilder.setDownTime(Long.valueOf(builder.getVal()));
				}
				return liBuilder;
			}
		}
		return null;
	}
	
	/**获取奖励预览
	 * @param playerId
	 * @param currentTermId
	 * @return
	 */
	public List<LimitLoginStatus> getLimitStatus(String playerId, int currentTermId){
		Optional<TimeLimitLoginEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return null;
		}
		TimeLimitLoginEntity entity = opEntity.get();
		List<LimitLoginStatus> list = new ArrayList<>();
		
		ConfigIterator<TimeLimitLoginAwardCfg> aConfigIterator = HawkConfigManager.getInstance().getConfigIterator(TimeLimitLoginAwardCfg.class);
		//遍历每一条
		while (aConfigIterator.hasNext()) {
			TimeLimitLoginAwardCfg cfg = aConfigIterator.next();
			//过滤下不满足的天数(防止配错表...)
			if (cfg.getDay() > getDifDaysStartToEnd()) {
				continue;
			}
			int id = cfg.getId();
			LimitLoginStatus.Builder builder = LimitLoginStatus.newBuilder();
			builder.setId(id);
			int status = entity.containStatus(id) ? HAVE_RECEIVED : NO_RECEIVE;
			builder.setStatus(status);
			boolean isOverTime = false;
			if (currentTermId <= 0 || id < currentTermId) {
				isOverTime =  true;
			}
			builder.setIsOver(isOverTime);
			list.add(builder.build());
		}
		return list;
	}
	
	/**计算开始和结束时间的间隔天数
	 * @return
	 */
	public int getDifDaysStartToEnd(){
		int termId = getActivityTermId();
		Date startDate = HawkTime.getAM0Date(new Date(getTimeControl().getStartTimeByTermId(termId)));
		Date endDate = HawkTime.getAM0Date(new Date(getTimeControl().getEndTimeByTermId(termId) + HawkTime.DAY_MILLI_SECONDS));
		int difDays = HawkTime.calcBetweenDays(startDate, endDate);
		return difDays;
	}
	
	
	/**
	 * 通过表里的数据获取时间范围和状态
	 * @param tKvCfg
	 * @param timeId
	 * @param startTimeAct
	 * @param day
	 * @return
	 */
	public KeyValuePairStr.Builder getTimeInfo(int id, long startTimeAct){
		//开始时间
		long startTime = getDownTimeByIdAndType(id, startTimeAct, true);
		//结束时间
		long endTime = getDownTimeByIdAndType(id, startTimeAct, false);
		//当前时间
		long now  = HawkTime.getMillisecond();
		KeyValuePairStr.Builder builder = KeyValuePairStr.newBuilder();
		if (now >= startTime && now <= endTime) {
			//在范围内
			builder.setKey(IN_STATUS);
			builder.setVal(String.valueOf(endTime));
			return builder;
		}else if (now < startTime) {
			//在开始之前
			builder.setKey(BEFORE_STATUS);
			builder.setVal(String.valueOf(startTime));
			return builder;
		}
		/*else if (now > endTime) {
			//在结束之后,, 就是下一轮的显示了
			return null;
		}*/
		return null;
	}
	
	
	/**
	 * 获取当前期数的开始截点时间/结束截点时间
	 * @param id
	 * @param startTimeAct
	 * @param isStartType
	 * @return
	 */
	public long getDownTimeByIdAndType(int id, long startTimeAct, boolean isStartType){
		//每期
		TimeLimitLoginAwardCfg tAwardCfg = HawkConfigManager.getInstance().getConfigByKey(TimeLimitLoginAwardCfg.class, id);
		if (tAwardCfg == null) {
			logger.info("getDownTimeByIdAndType, TimeLimitLoginAwardCfg == null id = ", id);
			return 0;
		}
		TimeLimitLoginKVCfg tKvCfg = HawkConfigManager.getInstance().getKVInstance(TimeLimitLoginKVCfg.class);
		String startTimeStr = "";
		if (isStartType) {
			startTimeStr = tKvCfg.getRewardTimeById(tAwardCfg.getTime()).getKey();
		}else{
			startTimeStr = tKvCfg.getRewardTimeById(tAwardCfg.getTime()).getValue();
		}
		List<Integer> startDayList = SerializeHelper.cfgStr2List(startTimeStr, ":");
		int startHour = startDayList.get(0);
		int startMin = startDayList.get(1);
		int days = tAwardCfg.getDay() - 1;
		long startTime = getNextTime(startTimeAct, days, startHour, startMin);
		return startTime;
	}
	/**
	 * 延后 *天*小时*分钟的时间点
	 * @param beginTime
	 * @param days
	 * @param hours
	 * @param minutes
	 * @return
	 */
	public long getNextTime(long beginTime, int days, int hours , int minutes){
		long startTime = beginTime + days * HawkTime.DAY_MILLI_SECONDS + hours * HawkTime.HOUR_MILLI_SECONDS + minutes * HawkTime.MINUTE_MILLI_SECONDS;
		return startTime;
	}
	@Override
	public void onPlayerMigrate(String playerId) {
		
	}
	
	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}
	
}

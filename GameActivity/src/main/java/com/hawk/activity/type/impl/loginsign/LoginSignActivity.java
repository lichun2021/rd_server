package com.hawk.activity.type.impl.loginsign;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.PlayerSignEvent;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.loginsign.cfg.LoginSignActivityCfg;
import com.hawk.activity.type.impl.loginsign.entity.ActivityLoginSignEntity;
import com.hawk.game.protocol.Activity.LoginSignInfoSync;
import com.hawk.game.protocol.Activity.LoginSignInfoSync.Builder;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.log.Action;

/**
 * 登录签到活动
 * 
 * @author PhilChen
 *
 */
public class LoginSignActivity extends ActivityBase {
	
	public LoginSignActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.LOGIN_SIGN_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		return new LoginSignActivity(config.getActivityId(), activityEntity);
	}

	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		// 获取玩家活动数据
		String playerId = event.getPlayerId();
		Optional<ActivityLoginSignEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		ActivityLoginSignEntity dataEntity = opDataEntity.get();
		if (dataEntity.getLastTookTime() <= 0) {
			return;
		}
		if (event.isCrossDay()) {
			dataEntity.setLastTookTime(0);
			dataEntity.setTookItemId(0);
			syncActivityInfo(dataEntity);
		}
	}
	
	private void syncActivityInfo(ActivityLoginSignEntity entity) {
		String playerId = entity.getPlayerId();
		LoginSignActivityCfg todayRewardConfig = getTodayRewardConfig(playerId);
		if (todayRewardConfig == null) {
			return;
		}
		Builder builder = LoginSignInfoSync.newBuilder();
		builder.setCurDay(todayRewardConfig.getDay());
		builder.setItemId(entity.getTookItemId());
		pushToPlayer(playerId, HP.code.PUSH_LOGIN_SIGN_INFO_SYNC_S_VALUE, builder);
	}
	
	private int getCurrentDay(String playerId) {
		Long createTime = getDataGeter().getPlayerCreateTime(playerId);
		long now = HawkTime.getMillisecond();
		int betweenDays = getBetweenDays(now, createTime);
		return betweenDays + 1;
	}
	
	/**
	 * 获取两个时间之间的天数差（存在闰年的问题）
	 * @param time1
	 * @param time2
	 * @return
	 */
	private int getBetweenDays(long time1, long time2) {
		if (time1 <= 0 || time2 <= 0 || time1 == time2) {
			return 0;
		}
		long beginTime, endTime;
		if (time1 > time2) {
			beginTime = time2;
			endTime = time1;
		} else {
			beginTime = time1;
			endTime = time2;
		}
		Date beginAm0 = HawkTime.getAM0Date(new Date(beginTime));
		Date endAm0 = HawkTime.getAM0Date(new Date(endTime));
		return (int) ((endAm0.getTime() - beginAm0.getTime()) / HawkTime.DAY_MILLI_SECONDS);
	}
	
	/**
	 * 获取今天的活动奖励配置
	 * @param playerId
	 * @return
	 */
	private LoginSignActivityCfg getTodayRewardConfig(String playerId) {
		int currentDay = getCurrentDay(playerId);
		Integer buildLevel = getDataGeter().getConstructionFactoryLevel(playerId);
		List<LoginSignActivityCfg> configList = LoginSignActivityCfg.getConfigList(buildLevel);
		if (configList.isEmpty()) {
			logger.info("login sign activity config list empty!  playerId: {}, cityLvl: {}", playerId, buildLevel);
			return null;
		}
		int day = currentDay % configList.size();
		if (day == 0) {
			return configList.get(configList.size() - 1);
		}
		for (LoginSignActivityCfg cfg : configList) {
			if (cfg.getDay() == day) {
				return cfg;
			}
		}
		logger.info("login sign activity config error! config not match! playerId: {}", playerId);
		return null;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<ActivityLoginSignEntity> queryList = HawkDBManager.getInstance()
				.query("from ActivityLoginSignEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			ActivityLoginSignEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}
	
	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		ActivityLoginSignEntity entity = new ActivityLoginSignEntity(playerId, termId);
		return entity;
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<ActivityLoginSignEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		// 向玩家推送数据变更
		syncActivityInfo(opDataEntity.get());
	}

	/**
	 * 领取活动奖励
	 * @param playerId
	 * @param itemId
	 * @return
	 */
	public Result<?> takeRewards(String playerId) {
		Optional<ActivityLoginSignEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		
		ActivityLoginSignEntity dataEntity = opDataEntity.get();
		if (dataEntity.getTookItemId() > 0) {
			return Result.fail(Status.Error.ACTIVITY_REWARD_IS_TOOK_VALUE);
		}
		LoginSignActivityCfg config = getTodayRewardConfig(playerId);
		if (config == null) {
			return Result.fail(Status.Error.ACTIVITY_REWARD_IS_TOOK_VALUE);
		}
		dataEntity.setTookItemId(config.getItemId());
		dataEntity.setLastTookTime(HawkTime.getMillisecond());
		
		
		ActivityReward reward = new ActivityReward(config.getRewardList(), Action.ACTIVITY_REWARD_LOGIN_SIGN);
		reward.setAlert(true);
		postReward(playerId, reward);
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), config.getItemId());
		
		syncActivityInfo(dataEntity);
		// 签到事件
		ActivityManager.getInstance().postEvent(new PlayerSignEvent(playerId));
		return Result.success();
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

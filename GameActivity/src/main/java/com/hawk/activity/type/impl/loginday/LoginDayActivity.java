package com.hawk.activity.type.impl.loginday;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.LoginDayEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.loginday.cfg.LoginDayAchieveCfg;
import com.hawk.activity.type.impl.loginday.entity.ActivityLoginDayEntity;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.log.Action;

public class LoginDayActivity extends ActivityBase implements AchieveProvider {

	public LoginDayActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.LOGIN_DAY_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.ACTIVITY_REWARD_LOGIN_DAY;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		LoginDayActivity activity = new LoginDayActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<ActivityLoginDayEntity> queryList = HawkDBManager.getInstance()
				.query("from ActivityLoginDayEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			ActivityLoginDayEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		ActivityLoginDayEntity entity = new ActivityLoginDayEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onOpenForPlayer(String playerId) {
		Optional<ActivityLoginDayEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		initAchieveInfo(playerId);
	}
	
	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<ActivityLoginDayEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		ActivityLoginDayEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		ConfigIterator<LoginDayAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(LoginDayAchieveCfg.class);
		while (configIterator.hasNext()) {
			LoginDayAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
		ActivityManager.getInstance().postEvent(new LoginDayEvent(playerId, entity.getLoginDays()), true);
	}
	
	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void clearExtraAchieveInfo(String playerId) {
		Optional<ActivityLoginDayEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		ActivityLoginDayEntity entity = opEntity.get();
		List<AchieveItem> items = entity.getItemList();
		Map<Integer, AchieveItem> itemMap = new HashMap<>();
		for (AchieveItem item : items) {
			int achieveId = item.getAchieveId();
			if (itemMap.containsKey(achieveId)) {
				AchieveItem oldItem = itemMap.get(achieveId);
				if (item.getState() > oldItem.getState()) {
					itemMap.put(achieveId, item);
				}
			} else {
				itemMap.put(achieveId, item);
			}
		}
		List<AchieveItem> newList = new ArrayList<>(itemMap.values());
		entity.setItemList(newList);
		entity.notifyUpdate();
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
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
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<ActivityLoginDayEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if(!opPlayerDataEntity.isPresent()){
			return Optional.empty();
		}
		ActivityLoginDayEntity playerDataEntity = opPlayerDataEntity.get();
		ConfigIterator<LoginDayAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(LoginDayAchieveCfg.class);
		int achieveSize = configIterator.size();
		if (playerDataEntity.getItemList().isEmpty()) {
			initAchieveInfo(playerId);
		} else if (playerDataEntity.getItemList().size() > achieveSize) {
			clearExtraAchieveInfo(playerId);
		}
		AchieveItems items = new AchieveItems(playerDataEntity.getItemList(), playerDataEntity);
		return Optional.of(items);
	}
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(LoginDayAchieveCfg.class, achieveId);
		return config;
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		ActivityManager.getInstance().getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}
	
	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		Optional<ActivityLoginDayEntity> opEntity = getPlayerDataEntity(playerId);
		ActivityLoginDayEntity entity = opEntity.get();
		if (event.isCrossDay() && !HawkTime.isSameDay(entity.getRefreshTime(), HawkTime.getMillisecond())) {
			entity.setLoginDays(entity.getLoginDays() + 1);
			entity.setRefreshTime(HawkTime.getMillisecond());
			ActivityManager.getInstance().postEvent(new LoginDayEvent(playerId, entity.getLoginDays()), true);
		}

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

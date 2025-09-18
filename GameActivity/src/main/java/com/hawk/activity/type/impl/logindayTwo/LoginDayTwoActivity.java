package com.hawk.activity.type.impl.logindayTwo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.result.Result;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.LoginDayTwoEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.logindayTwo.cfg.LoginDayTwoAchieveCfg;
import com.hawk.activity.type.impl.logindayTwo.entity.ActivityLoginDayTwoEntity;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

public class LoginDayTwoActivity extends ActivityBase implements AchieveProvider {

	public LoginDayTwoActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.LOGIN_DAY_TWO_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.ACTIVITY_REWARD_LOGIN_DAY_TWO;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		LoginDayTwoActivity activity = new LoginDayTwoActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<ActivityLoginDayTwoEntity> queryList = HawkDBManager.getInstance()
				.query("from ActivityLoginDayTwoEntity where playerId = ? and termId = ? and invalid = 0 order by createTime desc limit 1", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			ActivityLoginDayTwoEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		ActivityLoginDayTwoEntity entity = new ActivityLoginDayTwoEntity(playerId, termId);
		return entity;
	}
	

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_LOGIN_DAY_TWO, ()-> {
				initAchieveInfo(playerId);
			});
		}
	}
	
	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<ActivityLoginDayTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		ActivityLoginDayTwoEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		ConfigIterator<LoginDayTwoAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(LoginDayTwoAchieveCfg.class);
		while (configIterator.hasNext()) {
			LoginDayTwoAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
		ActivityManager.getInstance().postEvent(new LoginDayTwoEvent(playerId, 1), true);
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
		Optional<ActivityLoginDayTwoEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if(!opPlayerDataEntity.isPresent()){
			return Optional.empty();
		}
		ActivityLoginDayTwoEntity playerDataEntity = opPlayerDataEntity.get();
		if(playerDataEntity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		AchieveItems items = new AchieveItems(playerDataEntity.getItemList(), playerDataEntity);
		return Optional.of(items);
	}
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(LoginDayTwoAchieveCfg.class, achieveId);
		if (config == null) {
			config = HawkConfigManager.getInstance().getConfigByKey(LoginDayTwoAchieveCfg.class, achieveId);
		}
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
		Optional<ActivityLoginDayTwoEntity> opEntity = getPlayerDataEntity(playerId);
		ActivityLoginDayTwoEntity entity = opEntity.get();
		if (event.isCrossDay() || entity.getLoginDays() == 0) {
			entity.setLoginDays(entity.getLoginDays() + 1);
			ActivityManager.getInstance().postEvent(new LoginDayTwoEvent(playerId, entity.getLoginDays()), true);
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

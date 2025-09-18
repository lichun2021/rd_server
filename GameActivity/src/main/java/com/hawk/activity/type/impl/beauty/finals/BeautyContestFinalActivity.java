package com.hawk.activity.type.impl.beauty.finals;

import java.util.ArrayList;
import java.util.List;
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
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.beauty.finals.cfg.BeautyContestFinalAchieveCfg;
import com.hawk.activity.type.impl.beauty.finals.cfg.BeautyContestFinalKVCfg;
import com.hawk.activity.type.impl.beauty.finals.entity.BeautyContestFinalEntity;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.log.Action;

/**
 * 选美决赛
 * 
 * @author lating
 *
 */
public class BeautyContestFinalActivity extends ActivityBase implements AchieveProvider {

	public BeautyContestFinalActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.BEAUTY_CONTEST_FINAL_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.BEAUTY_FINALS_REWARD;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		BeautyContestFinalActivity activity = new BeautyContestFinalActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<BeautyContestFinalEntity> queryList = HawkDBManager.getInstance()
				.query("from BeautyContestFinalEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			BeautyContestFinalEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		BeautyContestFinalEntity entity = new BeautyContestFinalEntity(playerId, termId);
		return entity;
	}
	
	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<BeautyContestFinalEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		BeautyContestFinalEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		
		// 初始添加成就项
		ConfigIterator<BeautyContestFinalAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(BeautyContestFinalAchieveCfg.class);
		while (configIterator.hasNext()) {
			BeautyContestFinalAchieveCfg cfg = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			entity.addItem(item);
		}
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}

	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		
		Optional<BeautyContestFinalEntity> opEntity = getPlayerDataEntity(playerId);
		BeautyContestFinalEntity entity = opEntity.get();
		if (!event.isCrossDay() && !HawkTime.isCrossDay(
				HawkTime.getMillisecond(), entity.getLoginTime(), 0)) {
			return;
		}
		
		if(entity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		entity.setLoginTime(HawkTime.getMillisecond());
		BeautyContestFinalKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(BeautyContestFinalKVCfg.class);
		if (kvCfg.getDailyRefresh() == 0) {
			return;
		}
		
		List<AchieveItem> newItems = new ArrayList<AchieveItem>();
		// 初始添加成就项
		ConfigIterator<BeautyContestFinalAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(BeautyContestFinalAchieveCfg.class);
		while (configIterator.hasNext()) {
			BeautyContestFinalAchieveCfg cfg = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			newItems.add(item);
		}
		
		entity.resetItemList(newItems);
		entity.notifyUpdate();
		
		AchievePushHelper.pushAchieveUpdate(playerId, entity.getItemList());
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
		if (!isOpening(playerId)) {
			return Optional.empty();
		}
		
		Optional<BeautyContestFinalEntity> entityOp = getPlayerDataEntity(playerId);
		if(!entityOp.isPresent()){
			return Optional.empty();
		}
		BeautyContestFinalEntity entity = entityOp.get();
		if(entity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		AchieveItems items = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(items);
	}
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(BeautyContestFinalAchieveCfg.class, achieveId);
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		ActivityManager.getInstance().getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}
}

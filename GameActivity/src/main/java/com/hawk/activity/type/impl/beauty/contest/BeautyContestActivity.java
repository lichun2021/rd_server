package com.hawk.activity.type.impl.beauty.contest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.beauty.contest.cfg.BeautyContestAchieveCfg;
import com.hawk.activity.type.impl.beauty.contest.cfg.BeautyContestKVCfg;
import com.hawk.activity.type.impl.beauty.contest.entity.BeautyContestEntity;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.log.Action;

/**
 * 选美初赛
 * 
 * @author lating
 *
 */
public class BeautyContestActivity extends ActivityBase implements AchieveProvider {

	public BeautyContestActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.BEAUTY_CONTEST_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.BEAUTY_CONTEST_REWARD;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		BeautyContestActivity activity = new BeautyContestActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<BeautyContestEntity> queryList = HawkDBManager.getInstance()
				.query("from BeautyContestEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			BeautyContestEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		BeautyContestEntity entity = new BeautyContestEntity(playerId, termId);
		return entity;
	}
	
	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<BeautyContestEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		BeautyContestEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		
		// 初始添加成就项
		ConfigIterator<BeautyContestAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(BeautyContestAchieveCfg.class);
		while (configIterator.hasNext()) {
			BeautyContestAchieveCfg cfg = configIterator.next();
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
		
		Optional<BeautyContestEntity> opEntity = getPlayerDataEntity(playerId);
		BeautyContestEntity entity = opEntity.get();
		if (!event.isCrossDay() && !HawkTime.isCrossDay(
				HawkTime.getMillisecond(), entity.getLoginTime(), 0)) {
			return;
		}
		
		if(entity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		entity.setLoginTime(HawkTime.getMillisecond());
		BeautyContestKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(BeautyContestKVCfg.class);
		if (kvCfg.getDailyRefresh() == 0) {
			return;
		}
		
		List<AchieveItem> newItems = new ArrayList<AchieveItem>();
		// 初始添加成就项
		ConfigIterator<BeautyContestAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(BeautyContestAchieveCfg.class);
		while (configIterator.hasNext()) {
			BeautyContestAchieveCfg cfg = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			newItems.add(item);
		}
		
		entity.resetItemList(newItems);
		entity.notifyUpdate();
		
		// 推送给客户端
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
		
		Optional<BeautyContestEntity> entityOp = getPlayerDataEntity(playerId);
		if(!entityOp.isPresent()){
			return Optional.empty();
		}
		BeautyContestEntity entity = entityOp.get();
		if(entity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		AchieveItems items = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(items);
	}
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(BeautyContestAchieveCfg.class, achieveId);
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

	/**
	 * 购买道具
	 * 
	 * @param playerId
	 * @param count
	 * @return
	 */
	public Result<?> buyFlower(String playerId, int count) {
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		
		if (count <= 0) {
			return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
		}
		
		BeautyContestKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BeautyContestKVCfg.class);
		if (cfg == null) {
			return Result.fail(Status.SysError.CONFIG_ERROR_VALUE);
		}
		
		RewardItem.Builder consumeItem = RewardHelper.toRewardItem(cfg.getBuyItem());
		consumeItem.setItemCount(consumeItem.getItemCount() * count);
		List<RewardItem.Builder> consume = ImmutableList.of(consumeItem);
		boolean cost = this.getDataGeter().cost(playerId, consume, 1, Action.BEAUTY_BUY_FLOWER_CONSUME, true);
		if (!cost) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		
		RewardItem.Builder rewardBuilder = RewardHelper.toRewardItem(cfg.getItem());
		rewardBuilder.setItemCount(rewardBuilder.getItemCount() * count);
		List<RewardItem.Builder> rewardItem = ImmutableList.of(rewardBuilder);
		this.getDataGeter().takeReward(playerId, rewardItem, 1, Action.BEAUTY_BUY_FLOWER_AWARD, true, RewardOrginType.ACTIVITY_REWARD);
		
		return Result.success();
	}
	
}

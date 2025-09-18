package com.hawk.activity.type.impl.accumulateConsume;

import java.util.ArrayList;
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
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.accumulateConsume.cfg.AccumulateConsumeAchieveCfg;
import com.hawk.activity.type.impl.accumulateConsume.cfg.AccumulateConsumeActivityKVCfg;
import com.hawk.activity.type.impl.accumulateConsume.entity.AccumulateConsumeEntity;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 累积消耗活动
 * @author Jesse
 *
 */
public class AccumulateConsumeActivity extends ActivityBase implements AchieveProvider {

	public AccumulateConsumeActivity(int activityId, ActivityEntity activityEntity) {
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
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_ACCUMULATE_CONSUME, ()-> {
				initAchieveInfo(playerId);
			});
		}
	}
	
	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		AccumulateConsumeActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(AccumulateConsumeActivityKVCfg.class);
		// 不需要跨天重置
		if (!kvCfg.isDailyRefresh()) {
			return;
		}
		
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		
		if (!event.isCrossDay()) {
			return;
		}
		String playerId = event.getPlayerId();
		
		Optional<AccumulateConsumeEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		
		AccumulateConsumeEntity dataEntity = opDataEntity.get();
		List<AchieveItem> items = new ArrayList<>();
		ConfigIterator<AccumulateConsumeAchieveCfg> achieveIterator = HawkConfigManager.getInstance().getConfigIterator(AccumulateConsumeAchieveCfg.class);
		while (achieveIterator.hasNext()) {
			AccumulateConsumeAchieveCfg cfg = achieveIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			items.add(item);
		}
		dataEntity.resetItemList(items);
		// 推送给客户端
		AchievePushHelper.pushAchieveUpdate(playerId, dataEntity.getItemList());
		
	}
	

	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<AccumulateConsumeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		AccumulateConsumeEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		ConfigIterator<AccumulateConsumeAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(AccumulateConsumeAchieveCfg.class);
		while (configIterator.hasNext()) {
			AccumulateConsumeAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}

		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<AccumulateConsumeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		AccumulateConsumeEntity entity = opEntity.get();
		if(entity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public AccumulateConsumeAchieveCfg getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(AccumulateConsumeAchieveCfg.class, achieveId);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.ACCUMULATE_CONSUME_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.ACTIVITY_ACCUMULATE_CONSUME_AWARD;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		AccumulateConsumeActivity activity = new AccumulateConsumeActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<AccumulateConsumeEntity> queryList = HawkDBManager.getInstance()
				.query("from AccumulateConsumeEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			AccumulateConsumeEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		AccumulateConsumeEntity entity = new AccumulateConsumeEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
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

	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

}

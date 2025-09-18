package com.hawk.activity.type.impl.accumulateRechargeTwo;

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
import com.hawk.activity.type.impl.accumulateRechargeTwo.cfg.AccumulateRechargeTwoAchieveCfg;
import com.hawk.activity.type.impl.accumulateRechargeTwo.cfg.AccumulateRechargeTwoActivityKVCfg;
import com.hawk.activity.type.impl.accumulateRechargeTwo.entity.AccumulateRechargeTwoEntity;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 累积充值活动2(每日重置)
 */
public class AccumulateRechargeTwoActivity extends ActivityBase implements AchieveProvider {

	public AccumulateRechargeTwoActivity(int activityId, ActivityEntity activityEntity) {
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
			callBack(playerId, MsgId.ACHIEVE_INIT_ACCUMULATE_RECHARGE_TWO, ()-> {
				initAchieveInfo(playerId);
			});
		}
	}
	
	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<AccumulateRechargeTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		AccumulateRechargeTwoEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		ConfigIterator<AccumulateRechargeTwoAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(AccumulateRechargeTwoAchieveCfg.class);
		while (configIterator.hasNext()) {
			AccumulateRechargeTwoAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}

		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<AccumulateRechargeTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		AccumulateRechargeTwoEntity entity = opEntity.get();
		if(entity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	@Override
	public AccumulateRechargeTwoAchieveCfg getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(AccumulateRechargeTwoAchieveCfg.class, achieveId);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.ACCUMULATE_RECHARGE_TWO_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.ACTIVITY_ACCUMULATE_RECHARGE_TWO_AWARD;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		AccumulateRechargeTwoActivity activity = new AccumulateRechargeTwoActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<AccumulateRechargeTwoEntity> queryList = HawkDBManager.getInstance()
				.query("from AccumulateRechargeTwoEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			AccumulateRechargeTwoEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		AccumulateRechargeTwoEntity entity = new AccumulateRechargeTwoEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
	}
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		Optional<AccumulateRechargeTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		AccumulateRechargeTwoActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(AccumulateRechargeTwoActivityKVCfg.class);
		if(kvCfg.isDailyRefresh()){
			if (!event.isCrossDay()) {
				return;
			}
			List<AchieveItem> items = new ArrayList<>();
			ConfigIterator<AccumulateRechargeTwoAchieveCfg> achieveIterator = HawkConfigManager.getInstance().getConfigIterator(AccumulateRechargeTwoAchieveCfg.class);
			while (achieveIterator.hasNext()) {
				AccumulateRechargeTwoAchieveCfg cfg = achieveIterator.next();
				AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
				items.add(item);
			}
			AccumulateRechargeTwoEntity entity = opEntity.get();
			entity.resetItemList(items);
			// 推送给客户端
			AchievePushHelper.pushAchieveUpdate(playerId, entity.getItemList());
		}	
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

package com.hawk.activity.type.impl.powerfund;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.result.Result;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.BuyFundEvent;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.powerfund.cfg.PowerFundAchieveCfg;
import com.hawk.activity.type.impl.powerfund.cfg.PowerFundActivityKVCfg;
import com.hawk.activity.type.impl.powerfund.entity.PowerFundEntity;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.game.protocol.Activity.PowerFundInfoSync;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.log.Action;

/**
 * 战力基金活动
 * @author Jesse
 *
 */
public class PowerFundActivity extends ActivityBase implements AchieveProvider {

	public PowerFundActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public boolean isProviderActive(String playerId) {
		return isOpening(playerId);
	}
	
	@Override
	public boolean isProviderNeedSync(String playerId) {
		return isShow(playerId);
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		syncActivityDataInfo(playerId);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<PowerFundEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return Optional.empty();
		}
		PowerFundEntity entity = opEntity.get();
		AchieveItems items = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(items);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.POWER_FUND_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.ACTIVITY_POWER_FUND_AWARD;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		PowerFundActivity activity = new PowerFundActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<PowerFundEntity> queryList = HawkDBManager.getInstance()
				.query("from PowerFundEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			PowerFundEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		PowerFundEntity entity = new PowerFundEntity(playerId, termId);
		return entity;
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<PowerFundEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		PowerFundEntity entity = opEntity.get();
		PowerFundInfoSync.Builder builder = PowerFundInfoSync.newBuilder();
		builder.setIsBuy(entity.isBuy());
		pushToPlayer(playerId, HP.code.PUSH_POWERFUND_INFO_SYNC_S_VALUE, builder);
	}
	
	/**
	 * 直购基金（由付费直购改为金条购买）
	 * @param playerId
	 */
	public Result<?> buyPowerFund(String playerId) {
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		
		PowerFundActivityKVCfg kvConfig = HawkConfigManager.getInstance().getKVInstance(PowerFundActivityKVCfg.class);
		if (kvConfig == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		
		Optional<PowerFundEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		PowerFundEntity entity = opEntity.get();
		if (entity.isBuy()) {
			return Result.fail(Status.Error.ACTIVITY_POWERFUND_IS_BUY_VALUE);
		}
		
		// 购买消耗
		List<RewardItem.Builder> itemList = new ArrayList<>();
		itemList.add(RewardHelper.toRewardItem(kvConfig.getPrice()));
		boolean success = getDataGeter().consumeItems(playerId, itemList, HP.code.POWER_FUND_BUY_VALUE, Action.ACTIVITY_POWER_FUND_CONSUME);
		if (!success) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		
		entity.setIsBuy(true);
		// 添加成就数据项
		ConfigIterator<PowerFundAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(PowerFundAchieveCfg.class);
		List<AchieveItem> items = new ArrayList<>();
		while (configIterator.hasNext()) {
			PowerFundAchieveCfg config = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(config.getAchieveId());
			entity.addItem(item);
			items.add(item);
		}
		
		ActivityManager.getInstance().postEvent(new BuyFundEvent(playerId,this.getActivityType().intValue(), 0));
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, items), true);
		syncActivityDataInfo(playerId);
		getDataGeter().buyFundRecord(playerId, getActivityType());
		return Result.success();
	}
	
	/**
	 * 购买等级基金
	 * @param playerId
	 * @return
	 */
	
	/**
	 * 玩家是否购买战力基金
	 * @param playerId
	 * @return
	 */
	public boolean hasBuy(String playerId){
		Optional<PowerFundEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return false;
		}
		PowerFundEntity entity = opEntity.get();
		return entity.isBuy();
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(PowerFundAchieveCfg.class, achieveId);
	}

	@Override
	public boolean isActivityClose(String playerId) {
		Optional<PowerFundEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		PowerFundEntity entity = opEntity.get();
		if (!entity.isBuy()) {
			return false;
		}
		List<AchieveItem> itemList = entity.getItemList();
		if(itemList == null || itemList.isEmpty()){
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
		ActivityManager.getInstance().getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}
	
	@Override
	public void onTakeRewardSuccess(String playerId) {
		checkActivityClose(playerId);
	}

	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}

}

package com.hawk.activity.type.impl.growfund;

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
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.growfund.cfg.GrowFundActivityCfg;
import com.hawk.activity.type.impl.growfund.cfg.GrowFundActivityKVCfg;
import com.hawk.activity.type.impl.growfund.entity.GrowFundEntity;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.game.protocol.Activity.GrowfundInfoSync;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.log.Action;

/**
 * 等级基金活动
 * @author PhilChen
 *
 */
public class GrowFundActivity extends ActivityBase implements AchieveProvider {

	public GrowFundActivity(int activityId, ActivityEntity activityEntity) {
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
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<GrowFundEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return Optional.empty();
		}
		GrowFundEntity entity = opEntity.get();
		AchieveItems items = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(items);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.GROW_FUND_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.ACTIVITY_GROW_FUND_AWARD;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		GrowFundActivity activity = new GrowFundActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<GrowFundEntity> queryList = HawkDBManager.getInstance()
				.query("from GrowFundEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			GrowFundEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		GrowFundEntity entity = new GrowFundEntity(playerId, termId);
		return entity;
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<GrowFundEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		GrowFundEntity entity = opEntity.get();
		GrowfundInfoSync.Builder builder = GrowfundInfoSync.newBuilder();
		builder.setIsBuy(entity.isBuy());
		pushToPlayer(playerId, HP.code.PUSH_GROWFUND_INFO_SYNC_S_VALUE, builder);
	}

	/**
	 * 购买等级基金
	 * @param playerId
	 * @return
	 */
	public Result<?> buyGrowfund(String playerId) {
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		GrowFundActivityKVCfg kvConfig = HawkConfigManager.getInstance().getKVInstance(GrowFundActivityKVCfg.class);
		if (kvConfig == null) {
			return Result.fail(Status.Error.ACTIVITY_GROWFUND_CONFIG_NOT_FOUND_VALUE);
		}
		ActivityDataProxy dataGeter = getDataGeter();
		int vipLevel = dataGeter.getVipLevel(playerId);
		if (vipLevel < kvConfig.getLimitVipLevel()) {
			return Result.fail(Status.Error.ACTIVITY_GROWFUND_VIP_LEVEL_NOT_ENOUGH_VALUE);
		}
		Optional<GrowFundEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		GrowFundEntity entity = opEntity.get();
		if (entity.isBuy()) {
			return Result.fail(Status.Error.ACTIVITY_GROWFUND_IS_BUY_VALUE);
		}

		List<RewardItem.Builder> itemList = new ArrayList<>();
		itemList.add(RewardHelper.toRewardItem(kvConfig.getCostNum()));
		boolean consumeResult = getDataGeter().consumeItems(playerId, itemList, HP.code.GROW_FUND_BUY_VALUE, Action.ACTIVITY_GROW_FUND_CONSUME);
		if (consumeResult == false) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}

		entity.setIsBuy(true);
		// 添加成就数据项
		ConfigIterator<GrowFundActivityCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(GrowFundActivityCfg.class);
		List<AchieveItem> items = new ArrayList<>();
		while (configIterator.hasNext()) {
			GrowFundActivityCfg config = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(config.getAchieveId());
			entity.addItem(item);
			items.add(item);
		}
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, items), true);
		ActivityManager.getInstance().postEvent(new BuyFundEvent(playerId,this.getActivityType().intValue(), 0));
		syncActivityDataInfo(playerId);
		// 流水记录
		getDataGeter().buyFundRecord(playerId, getActivityType());
		return Result.success();
	}
	
	/**
	 * 玩家是否购买成长基金
	 * @param playerId
	 * @return
	 */
	public boolean hasBuy(String playerId){
		Optional<GrowFundEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return false;
		}
		GrowFundEntity entity = opEntity.get();
		return entity.isBuy();
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(GrowFundActivityCfg.class, achieveId);
	}

	@Override
	public boolean isActivityClose(String playerId) {
		Optional<GrowFundEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		GrowFundEntity entity = opEntity.get();
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		// TODO Auto-generated method stub
		
	}

}

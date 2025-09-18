package com.hawk.activity.type.impl.growfundnew;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.hawk.task.HawkTaskManager;
import org.hawk.xid.HawkXID;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.constant.ObjType;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.BuyFundEvent;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.msg.PlayerRewardFromActivityMsg;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.growfundnew.cfg.GrowFundNewActivityCfg;
import com.hawk.activity.type.impl.growfundnew.cfg.GrowFundNewActivityKVCfg;
import com.hawk.activity.type.impl.growfundnew.entity.GrowFundNewEntity;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.game.protocol.Activity.GrowfundNewInfoSync;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.log.Action;

/**
 * 新成长基金活动
 * 
 * @author lating
 *
 */
public class GrowFundNewActivity extends ActivityBase implements AchieveProvider {

	public GrowFundNewActivity(int activityId, ActivityEntity activityEntity) {
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
		Optional<GrowFundNewEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return Optional.empty();
		}
		
		GrowFundNewEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveInfo(playerId);
		}
		
		AchieveItems items = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(items);
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		if (!isOpening(playerId)) {
			return;
		}
		
		Optional<GrowFundNewEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		GrowFundNewEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveInfo(playerId);
		}
	}
	
	/**
	 * 初始化成就信息
	 * 
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<GrowFundNewEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		GrowFundNewEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
				
		List<AchieveItem> itemList = new ArrayList<AchieveItem>();
		// 初始添加成就项
		ConfigIterator<GrowFundNewActivityCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(GrowFundNewActivityCfg.class);
		while (configIterator.hasNext()) {
			GrowFundNewActivityCfg config = configIterator.next();
			if (config.getAchieveType() == AchieveType.BUILD_LEVEL_UP_NEW) {
				continue;
			}
			AchieveItem item = AchieveItem.valueOf(config.getAchieveId());
			entity.addItem(item);
			itemList.add(item);
		}

		entity.resetItemList(itemList);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);	
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.GROW_FUND_NEW_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.ACTIVITY_GROW_FUND_AWARD;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		GrowFundNewActivity activity = new GrowFundNewActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<GrowFundNewEntity> queryList = HawkDBManager.getInstance()
				.query("from GrowFundNewEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			GrowFundNewEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		GrowFundNewEntity entity = new GrowFundNewEntity(playerId, termId);
		return entity;
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<GrowFundNewEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		
		GrowFundNewEntity entity = opEntity.get();
		GrowfundNewInfoSync.Builder builder = GrowfundNewInfoSync.newBuilder();
		builder.setIsBuy(entity.isBuy());
		pushToPlayer(playerId, HP.code.GROW_FUND_NEW_INFO_SYNC_S_VALUE, builder);
	}

	/**
	 * 购买成长基金
	 * 
	 * @param playerId
	 * @return
	 */
	public Result<?> buyGrowfund(String playerId) {
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		
		GrowFundNewActivityKVCfg kvConfig = HawkConfigManager.getInstance().getKVInstance(GrowFundNewActivityKVCfg.class);
		if (kvConfig == null) {
			return Result.fail(Status.Error.ACTIVITY_GROWFUND_CONFIG_NOT_FOUND_VALUE);
		}
		
		int vipLevel = getDataGeter().getVipLevel(playerId);
		if (vipLevel < kvConfig.getLimitVipLevel()) {
			return Result.fail(Status.Error.ACTIVITY_GROWFUND_VIP_LEVEL_NOT_ENOUGH_VALUE);
		}
		Optional<GrowFundNewEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		GrowFundNewEntity entity = opEntity.get();
		if (entity.isBuy()) {
			return Result.fail(Status.Error.ACTIVITY_GROWFUND_IS_BUY_VALUE);
		}

		List<RewardItem.Builder> itemList = new ArrayList<>();
		itemList.add(RewardHelper.toRewardItem(kvConfig.getCostNum()));
		boolean success = getDataGeter().consumeItems(playerId, itemList, HP.code.GROW_FUND_NEW_BUY_VALUE, Action.ACTIVITY_GROW_FUND_CONSUME);
		if (!success) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}

		entity.setFundBuyTime(HawkTime.getMillisecond());
		// 添加成就数据项
		ConfigIterator<GrowFundNewActivityCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(GrowFundNewActivityCfg.class);
		List<AchieveItem> items = new ArrayList<>();
		while (configIterator.hasNext()) {
			GrowFundNewActivityCfg config = configIterator.next();
			if (config.getAchieveType() != AchieveType.BUILD_LEVEL_UP_NEW) {
				continue;
			}
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
	 * 一键领取奖励
	 * 
	 * @param playerId
	 * @return
	 */
	public Result<?> onekeyReward(String playerId) {
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		
		Optional<GrowFundNewEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}

		GrowFundNewEntity entity = opEntity.get();
		// 领取奖励
		takeAllAchieveReward(playerId, entity);
		
		return Result.success();
	}
	
	/**
	 * 玩家是否购买成长基金
	 * @param playerId
	 * @return
	 */
	public boolean hasBuy(String playerId){
		Optional<GrowFundNewEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return false;
		}
		
		GrowFundNewEntity entity = opEntity.get();
		return entity.isBuy();
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(GrowFundNewActivityCfg.class, achieveId);
	}
	
	/**
	 * 一键领取所有奖励
	 * 
	 * @param playerId
	 */
	public void takeAllAchieveReward(String playerId, GrowFundNewEntity entity) {
		List<RewardItem.Builder> reweardList = new ArrayList<RewardItem.Builder>();
		List<AchieveItem> items = new ArrayList<>();
		for (AchieveItem item : entity.getItemList()) {
			if (item.getState() != AchieveState.NOT_REWARD_VALUE) {
				continue;
			}
			
			GrowFundNewActivityCfg cfg = HawkConfigManager.getInstance().getConfigByKey(GrowFundNewActivityCfg.class, item.getAchieveId());
			if (cfg.getAchieveType() == AchieveType.BUILD_LEVEL_UP_NEW && !entity.isBuy()) {
				continue;
			}
			
			// 考虑奖励合并
			reweardList.addAll(cfg.getRewardList());
			item.setState(AchieveState.TOOK_VALUE);
			items.add(item);
			ActivityManager.getInstance().getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), item.getAchieveId());
		}
		
		entity.notifyUpdate();
		if (!reweardList.isEmpty()) {
			HawkXID xid = HawkXID.valueOf(ObjType.PLAYER, playerId);
			PlayerRewardFromActivityMsg msg = PlayerRewardFromActivityMsg.valueOf(reweardList, takeRewardAction(), true, RewardOrginType.ACTIVITY_REWARD, 0);
			HawkTaskManager.getInstance().postMsg(xid, msg);
		}
		
		AchievePushHelper.pushAchieveUpdate(playerId, items);
		onTakeRewardSuccess(playerId);
	}
	
	@Override
	public boolean isActivityClose(String playerId) {
		Optional<GrowFundNewEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		
		GrowFundNewEntity entity = opEntity.get();
		// 如果还没有购买豪华基金的权限，直接返回false
		if (!entity.isBuy()) {
			return false;
		} 
		if(entity.getItemList().isEmpty()){
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
		AchieveConfig cfg = getAchieveCfg(achieveId);
		if (cfg.getAchieveType() == AchieveType.BUILD_LEVEL_UP_NEW) {
			Optional<GrowFundNewEntity> opEntity = getPlayerDataEntity(playerId);
			if(opEntity.isPresent() && !opEntity.get().isBuy()){
				return Result.fail(Status.Error.NEED_BUY_GROWFUND_NEW_VALUE);
			}
		}
		
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

package com.hawk.activity.type.impl.celebrationShop;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import com.hawk.activity.event.impl.ActivityRewardsEvent;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTip;
import com.hawk.activity.type.impl.shareGlory.cfg.ShareGloryKVCfg;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.celebrationShop.cfg.CelebrationShopExchangeCfg;
import com.hawk.activity.type.impl.celebrationShop.entity.CelebrationShopEntity;
import com.hawk.game.protocol.Activity.CelebrationShopBean;
import com.hawk.game.protocol.Activity.CelebrationShopMainRes;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.log.Action;

public class CelebrationShopActivity extends ActivityBase implements AchieveProvider, IExchangeTip<CelebrationShopExchangeCfg> {

	public CelebrationShopActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.CELEBRATION_SHOP_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.CELEBRATION_SHOP_REWARD;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		CelebrationShopActivity activity = new CelebrationShopActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<CelebrationShopEntity> queryList = HawkDBManager.getInstance()
				.query("from CelebrationShopEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			CelebrationShopEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		CelebrationShopEntity entity = new CelebrationShopEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onOpenForPlayer(String playerId) {
		Optional<CelebrationShopEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<CelebrationShopEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if(opPlayerDataEntity.isPresent()){
			CelebrationShopEntity entity = opPlayerDataEntity.get();
			
			CelebrationShopMainRes.Builder builder = CelebrationShopMainRes.newBuilder();
			for (Entry<Integer,Integer> entry : entity.getExchangeMap().entrySet()) {
				CelebrationShopBean.Builder infoBuilder = CelebrationShopBean.newBuilder();
				infoBuilder.setItemId(entry.getKey());
				infoBuilder.setItemNum(entry.getValue());
				builder.addBeans(infoBuilder);
			}
			builder.addAllTips(getTips(CelebrationShopExchangeCfg.class, entity.getTipSet()));
			PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code.CELEBRATION_SHOP_MAIN_RES, builder));
		}
	}

	public void exchange(String playerId,int itemId,int itemNum){
		Optional<CelebrationShopEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if(opPlayerDataEntity.isPresent()){
			CelebrationShopEntity entity = opPlayerDataEntity.get();
		
			CelebrationShopExchangeCfg exchangeCfg = null;
			ConfigIterator<CelebrationShopExchangeCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(CelebrationShopExchangeCfg.class);
			while(configIterator.hasNext()){
				CelebrationShopExchangeCfg cfg = configIterator.next();
				if(cfg.getId() == itemId){
					exchangeCfg = cfg;
					break;
				}
			}
			if(exchangeCfg == null){
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.CELEBRATION_SHOP_EXCHANGE_REQ_VALUE,Status.Error.CELEBRATION_SHOP_ID_NOT_FIND_VALUE);
				return;
			}
			
			Integer number = entity.getExchangeMap().get(itemId);
			if(number == null)
				number = 0;
			if(number + itemNum > exchangeCfg.getExchangeCount()){
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.CELEBRATION_SHOP_EXCHANGE_REQ_VALUE,Status.Error.CELEBRATION_SHOP_NUMBER_LIMIT_ERROR_VALUE);
				return;
			}
			
			List<RewardItem.Builder> costItems = RewardHelper.toRewardItemList(exchangeCfg.getPay());
			//扣道具
			boolean flag = this.getDataGeter().cost(playerId, costItems, itemNum, Action.CELEBRATION_SHOP_EXCHANGE_COST, true);
			if (!flag) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.CELEBRATION_SHOP_EXCHANGE_REQ_VALUE,Status.Error.CELEBRATION_SHOP_EXCHANGE_LIMIT_ERROR_VALUE);
				return;
			}
			
			
			entity.putExchangeMap(itemId, number + itemNum);
			entity.notifyUpdate();
			
			//发奖励
			List<RewardItem.Builder> rewardItems = RewardHelper.toRewardItemList(exchangeCfg.getGain());
			this.getDataGeter().takeReward(playerId,rewardItems, itemNum, Action.CELEBRATION_SHOP_EXCHANGE_REWARD, true);
			//荣耀同享活动监控的事件
			if(!rewardItems.isEmpty()){
				RewardItem.Builder tmp = rewardItems.get(0);
				ActivityRewardsEvent event = new ActivityRewardsEvent(playerId, Action.CELEBRATION_SHOP_EXCHANGE_REWARD.intItemVal(),
						tmp.getItemId(), tmp.getItemType(),
						(int)tmp.getItemCount() * itemNum,
						ShareGloryKVCfg.DonateItemType.typeA);
				event.setExchangeId(exchangeCfg.getId());
				ActivityManager.getInstance().postEvent(event);
			}


			syncActivityDataInfo(playerId);
		}
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
	public AchieveConfig getAchieveCfg(int achieveId) {
		return null;
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
	
	@Override
	public void onPlayerLogin(String playerId) {
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		return Optional.empty();
	}
}

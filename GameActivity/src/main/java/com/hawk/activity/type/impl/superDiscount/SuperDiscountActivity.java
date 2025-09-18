package com.hawk.activity.type.impl.superDiscount;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.superDiscount.cfg.SuperDiscountKVCfg;
import com.hawk.activity.type.impl.superDiscount.cfg.SuperDiscountPoolCfg;
import com.hawk.activity.type.impl.superDiscount.cfg.SuperDiscountShopCfg;
import com.hawk.activity.type.impl.superDiscount.entity.SuperDiscountEntity;
import com.hawk.game.protocol.Activity.HPSuperDiscountInfoResp;
import com.hawk.game.protocol.Common.KeyValuePairInt;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;

public class SuperDiscountActivity extends ActivityBase {
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<SuperDiscountEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}

	public SuperDiscountActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.SUPER_DISCOUNT_ACTIVITY;
	}

	@Override
	public void onPlayerLogin(String playerId) {
		try {
			if (isOpening(playerId)) {
				Optional<SuperDiscountEntity> opDataEntity = this.getPlayerDataEntity(playerId);
				if (!opDataEntity.isPresent()) {
					return;
				}
				this.syncActivityInfo(playerId, opDataEntity.get());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	

	private void syncActivityInfo(String playerId, SuperDiscountEntity entity) {
		HPSuperDiscountInfoResp.Builder builder = HPSuperDiscountInfoResp.newBuilder();
		builder.setFreeTimes(this.getFressTimes(entity));
		builder.setDeadLine(entity.getDeadline());
		builder.setState(HawkTime.getMillisecond() < entity.getDeadline() ? 1 : 0);
		builder.setPoolId(entity.getPoolId());
		builder.setRefreshTimes(entity.getDrawTimes());
		// 如果状态是可买
		if (1 == builder.getState()) {
			for (Map.Entry<Integer, Integer> entry : entity.getBuyRecordMap().entrySet()) {
				KeyValuePairInt.Builder pair = KeyValuePairInt.newBuilder();
				pair.setKey(entry.getKey());
				pair.setVal(entry.getValue());
				builder.addGoods(pair);
			}
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code.SUPER_DISCOUNT_INFO_RESP_VALUE, builder));
	}
	
	private int getFressTimes(SuperDiscountEntity entity){
		SuperDiscountKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SuperDiscountKVCfg.class);
		return Math.max(0, cfg.getRefreshFree() - entity.getFreeTimes());
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		SuperDiscountActivity activity = new SuperDiscountActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<SuperDiscountEntity> queryList = HawkDBManager.getInstance()
				.query("from SuperDiscountEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			SuperDiscountEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		SuperDiscountEntity entity = new SuperDiscountEntity(playerId, termId);
		return entity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
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
		SuperDiscountKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SuperDiscountKVCfg.class);
		if(cfg.getRefresh() <= 0){
			return;
		}
		if (event.isCrossDay()) {
			Optional<SuperDiscountEntity> opEntity = this.getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return;
			}
			
			SuperDiscountEntity entity = opEntity.get();
			//刷新免费次数
			entity.setFreeTimes(0);
			entity.setDrawTimes(0);
			entity.notifyUpdate();			
			this.syncActivityDataInfo(playerId);
		}
	}
	
	
	void onProtocolActivityDrawReq(int protocolType, String playerId) {
		try {
			Optional<SuperDiscountEntity> opDataEntity = getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SUPER_DISCOUNT_NOT_OPEN_VALUE);
				return;
			}
			SuperDiscountEntity entity = opDataEntity.get();
			// 配置表
			SuperDiscountKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SuperDiscountKVCfg.class);
			if (null == cfg) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SUPER_DISCOUNT_NOT_EXISTS_VALUE);
				return;
			}
			int termId = this.getActivityTermId();
			// 免费次数
			boolean costFree = true;
			List<RewardItem.Builder> cost = new ArrayList<>();
			if (entity.getFreeTimes() >= cfg.getRefreshFree()) {
				cost.addAll(cfg.getNeedgoldList());
				costFree = false;
				int drawTimeLimit = cfg.getRefreshLimit();
				if(entity.getDrawTimes() >= drawTimeLimit){
					PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
							Status.Error.SUPER_DISCOUNT_REFRESH_TIMES_VALUE);
					return;
				}
			}
			int teamId = cfg.getNormalPoolRange();
			if (entity.getDrawAllTimes() < cfg.getPseudorandomNum()) {
				teamId = cfg.getRefreshPoolRange();
			}
			HawkTuple2<Integer, List<SuperDiscountPoolCfg>> poolCfg = SuperDiscountPoolCfg.getPoolWithTeamId(teamId);
			if (null == poolCfg) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SUPER_DISCOUNT_POOL_CFG_VALUE);
				return;
			}
			// 奖池内随机(先抽奖)
			int totalWeight = poolCfg.first;
			int poolId = 0;
			int cfgId = 0;
			int curWeight = HawkRand.randInt(1, totalWeight);
			SuperDiscountPoolCfg drawPoolCfg = null;
			for (SuperDiscountPoolCfg iterCfg : poolCfg.second) {
				if (iterCfg.getWeight() < curWeight) {
					curWeight -= iterCfg.getWeight();
					continue;
				}
				drawPoolCfg = iterCfg;
				poolId = iterCfg.getPool();
				cfgId = iterCfg.getId();
				break;
			}
			List<SuperDiscountShopCfg> shopGoodsCfg = SuperDiscountShopCfg.getShopsByGroup(poolId);
			if (null == shopGoodsCfg || null == drawPoolCfg) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SUPER_DISCOUNT_POOL_CFG_VALUE);
				return;
			}
			if (!cost.isEmpty()) {
				// 判断道具足够否
				boolean flag = this.getDataGeter().cost(playerId, cost, 1, Action.SUPER_DISCOUNT_DRAW_COST, false);
				if (!flag) {
					PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
							Status.Error.SUPER_DISCOUNT_DRAW_TIMES_VALUE);
					return;
				}
			}
			// 如果使用的是免费次数扣除免费次数
			if (costFree) {
				entity.setFreeTimes(entity.getFreeTimes() + 1);
			}else{
				entity.setDrawTimes(entity.getDrawTimes() + 1);
			}
			// 刷新奖池
			entity.getBuyRecordMap().clear();
			entity.setDrawAllTimes(entity.getDrawAllTimes() + 1);
			entity.setPoolId(poolId);
			entity.setDeadline(HawkTime.getMillisecond() + cfg.getResetTime());
			entity.notifyUpdate();
			// 操作成功
			PlayerPushHelper.getInstance().responseSuccess(playerId, protocolType);
			// 推送信息
			this.syncActivityInfo(playerId, entity);
			// 打点
			this.getDataGeter().logSuperDiscountDraw(playerId, termId,costFree? 1 : 2,cfgId, poolId, drawPoolCfg.getDiscount());

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	void onProtocolActivityBuyReq(int protocolType, String playerId, int cfgId, int count, int voucherId ) {
		try {
			Optional<SuperDiscountEntity> opDataEntity = getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SUPER_DISCOUNT_NOT_OPEN_VALUE);
				return;
			}
			SuperDiscountEntity entity = opDataEntity.get();

			if (HawkTime.getMillisecond() > entity.getDeadline()) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SUPER_DISCOUNT_POOL_CLOSE_VALUE);
				return;
			}
			// 验证奖池有效
			List<SuperDiscountShopCfg> shopGoodsCfg = SuperDiscountShopCfg.getShopsByGroup(entity.getPoolId());
			if (null == shopGoodsCfg) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SUPER_DISCOUNT_POOL_CFG_VALUE);
				return;
			}
			
			SuperDiscountPoolCfg poolCfg = HawkConfigManager.getInstance().getConfigByKey(SuperDiscountPoolCfg.class, entity.getPoolId());
			if(null == poolCfg){
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SUPER_DISCOUNT_POOL_CFG_VALUE);
				return;			
			}
			
			// 验证道具合法
			List<SuperDiscountShopCfg> findCfgs = shopGoodsCfg.stream().filter(e -> e.getId() == cfgId)
					.collect(Collectors.toList());
			if (null == findCfgs || 1 != findCfgs.size()) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SUPER_DISCOUNT_BUY_ITEM_CFG_VALUE);
				return;
			}
			// 验证道具限购次数
			SuperDiscountShopCfg shopItemCfg = findCfgs.get(0);

			int boughtTimes = entity.getBuyTimes(cfgId);
			if (boughtTimes + count > shopItemCfg.getLimitNum()) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SUPER_DISCOUNT_BUY_TIMES_VALUE);
				return;
			}
			// 验证道具足够
			// 判断道具足够否
			List<RewardItem.Builder> costList =  RewardHelper.toRewardItemList(shopItemCfg.getNewPrice());
			for(RewardItem.Builder costItem : costList){
				costItem.setItemCount(costItem.getItemCount() * count);
			}
			//使用代金券
			if(voucherId > 0){
				boolean canUse = shopItemCfg.canUseCashItem(voucherId);
				if(!canUse){
					PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, 
							protocolType,Status.Error.VOUCHER_UNAVAILABLE_VALUE);
					return;
				}
				int voucherItemNum = this.getDataGeter().getItemNum(playerId, voucherId);
				if(voucherItemNum <= 0){
					PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, 
							protocolType,Status.Error.ITEM_NOT_ENOUGH_VALUE);
					return;
				}
				int rlt = this.useVoucher(playerId, voucherId, costList);
				if(rlt >0){
					PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,rlt);
					return;
				}
			}
			
			boolean flag = this.getDataGeter().cost(playerId, costList, 1,
					Action.SUPER_DISCOUNT_BUY_COST, false);
			if (!flag) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.SUPER_DISCOUNT_BUY_GOLDEN_VALUE);
				return;
			}
			// 设置entity数据
			entity.addBuyTimes(cfgId, count);
			entity.notifyUpdate();
			// 发道具
			this.getDataGeter().takeReward(playerId, shopItemCfg.getItemList(), count, Action.SUPER_DISCOUNT_BUY_GAIN, true, RewardOrginType.ACTIVITY_REWARD);
			// 操作成功
			PlayerPushHelper.getInstance().responseSuccess(playerId, protocolType);
			this.syncActivityInfo(playerId, entity);
			
			int termId = this.getActivityTermId();
			// 打点
			this.getDataGeter().logSuperDiscountBuy(playerId, termId,shopItemCfg.getItem(), shopItemCfg.getNewPrice(), count, poolCfg.getDiscount(), shopItemCfg.getId(),voucherId);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	public int useVoucher(String playerId,int voucherId,List<RewardItem.Builder> costList){
		long giftPrice = 0;
		for (RewardItem.Builder itemInfo : costList) {
			if (itemInfo.getItemId() == PlayerAttr.DIAMOND_VALUE ) {
				giftPrice = itemInfo.getItemCount();
				break;
			}
		}
		if(giftPrice <= 0){
			return Status.Error.VOUCHER_UNAVAILABLE_VALUE;
		}
		long voucherLimitPrice = this.getDataGeter().getVoucherItemLimitPrice(voucherId, PlayerAttr.DIAMOND_VALUE);
		if (giftPrice < voucherLimitPrice) {
			return Status.Error.VOUCHER_UNAVAILABLE_VALUE;
		}
		long outTime = this.getDataGeter().getVoucherEndTime(voucherId);
		if (HawkTime.getMillisecond() > outTime) {
			return Status.Error.VOUCHER_OVERTIME_VALUE;
		}
		
		long voucherValue = this.getDataGeter().getVoucherValue(voucherId);
		for (RewardItem.Builder costItem : costList) {
			if (costItem.getItemId() == PlayerAttr.DIAMOND_VALUE) {
				long value = (costItem.getItemCount() - voucherValue) > 0 ? (costItem.getItemCount() - voucherValue) : 0;
				costItem.setItemCount(value);
				break;
			}
		}
		RewardItem.Builder voucherItem = RewardItem.newBuilder();
		voucherItem.setItemType(ItemType.TOOL_VALUE);
		voucherItem.setItemId(voucherId);
		voucherItem.setItemCount(1);
		costList.add(voucherItem);
		return 0;
	}
	
	
	
}

package com.hawk.activity.type.impl.luckyDiscount;

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
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.luckyDiscount.cfg.LuckyDiscountKVCfg;
import com.hawk.activity.type.impl.luckyDiscount.cfg.LuckyDiscountPoolCfg;
import com.hawk.activity.type.impl.luckyDiscount.cfg.LuckyDiscountShopCfg;
import com.hawk.activity.type.impl.luckyDiscount.entity.LuckyDiscountEntity;
import com.hawk.game.protocol.Activity.HPLuckDiscountInfoSync;
import com.hawk.game.protocol.Common.KeyValuePairInt;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;

public class LuckyDiscountActivity extends ActivityBase {
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<LuckyDiscountEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}

	public LuckyDiscountActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.LUCKY_DISCOUNT_ACTIVITY;
	}

	@Override
	public void onPlayerLogin(String playerId) {
		try {
			if (isOpening(playerId)) {
				Optional<LuckyDiscountEntity> opDataEntity = this.getPlayerDataEntity(playerId);
				if (!opDataEntity.isPresent()) {
					return;
				}
				this.syncActivityInfo(playerId, opDataEntity.get());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private void syncActivityInfo(String playerId, LuckyDiscountEntity entity) {
		HPLuckDiscountInfoSync.Builder builder = HPLuckDiscountInfoSync.newBuilder();
		builder.setFreeTimes(entity.getFreeTimes());
		builder.setDeadLine(entity.getDeadline());
		builder.setState(HawkTime.getMillisecond() < entity.getDeadline() ? 1 : 0);
		builder.setPoolId(entity.getPoolId());

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
				HawkProtocol.valueOf(HP.code.HP_LUCKY_DISCOUNT_INFO_SYNC_S_VALUE, builder));
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		LuckyDiscountActivity activity = new LuckyDiscountActivity(config.getActivityId(), activityEntity);

		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<LuckyDiscountEntity> queryList = HawkDBManager.getInstance()
				.query("from LuckyDiscountEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			LuckyDiscountEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		LuckyDiscountEntity entity = new LuckyDiscountEntity(playerId, termId);
		entity.setFreeTimes(1);
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
		if (event.isCrossDay()) {
			Optional<LuckyDiscountEntity> opEntity = this.getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return;
			}
			
			LuckyDiscountEntity entity = opEntity.get();
			//刷新免费次数
			entity.setFreeTimes(1);
			entity.notifyUpdate();			
			this.syncActivityDataInfo(playerId);
		}
	}
	void onProtocolActivityDrawReq(int protocolType, String playerId) {
		try {
			Optional<LuckyDiscountEntity> opDataEntity = getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.LUCKY_DISCOUNT_NOT_OPEN_VALUE);
				return;
			}
			LuckyDiscountEntity entity = opDataEntity.get();
			// 配置表
			LuckyDiscountKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LuckyDiscountKVCfg.class);
			if (null == cfg) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.LUCKY_DISCOUNT_NOT_EXISTS_VALUE);
				return;
			}
			// 免费次数
			int costFreeTimes = 1;
			List<RewardItem.Builder> cost = new ArrayList<>();
			if (entity.getFreeTimes() <= 0) {
				cost.addAll(cfg.getNeedItemList());
				costFreeTimes = 0;
			}
			int teamId = cfg.getNormalPoolRange();
			if (entity.getDrawTimes() <= cfg.getPseudorandomNum()) {
				teamId = cfg.getRefreshPoolRange();
			}
			HawkTuple2<Integer, List<LuckyDiscountPoolCfg>> poolCfg = LuckyDiscountPoolCfg.getPoolWithTeamId(teamId);
			if (null == poolCfg) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.LUCKY_DISCOUNT_POOL_CFG_VALUE);
				return;
			}
			// 奖池内随机(先抽奖)
			int totalWeight = poolCfg.first;
			int poolId = 0;
			int curWeight = HawkRand.randInt(1, totalWeight);
			LuckyDiscountPoolCfg drawPoolCfg = null;
			for (LuckyDiscountPoolCfg iterCfg : poolCfg.second) {
				if (iterCfg.getWeight() < curWeight) {
					curWeight -= iterCfg.getWeight();
					continue;
				}
				drawPoolCfg = iterCfg;
				poolId = iterCfg.getPool();
				break;
			}
			List<LuckyDiscountShopCfg> shopGoodsCfg = LuckyDiscountShopCfg.getShopsByGroup(poolId);
			if (null == shopGoodsCfg || null == drawPoolCfg) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.LUCKY_DISCOUNT_POOL_CFG_VALUE);
				return;
			}
			if (!cost.isEmpty()) {
				// 判断道具足够否
				boolean flag = this.getDataGeter().cost(playerId, cost, 1, Action.LUCKY_DISCOUNT_DRAW_COST, false);
				if (!flag) {
					PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
							Status.Error.LUCKY_DISCOUNT_DRAW_TIMES_VALUE);
					return;
				}
			}
			// 如果使用的是免费次数扣除免费次数
			if (costFreeTimes > 0) {
				entity.setFreeTimes(entity.getFreeTimes() > 0 ? entity.getFreeTimes() - 1 : 0);
			}
			// 刷新奖池
			entity.getBuyRecordMap().clear();
			entity.setDrawTimes(entity.getDrawTimes() + 1);
			entity.setPoolId(poolId);
			entity.setDeadline( HawkTime.getMillisecond() + cfg.getResetTime() );
			entity.notifyUpdate();
			// 操作成功
			PlayerPushHelper.getInstance().responseSuccess(playerId, protocolType);
			// 推送信息
			this.syncActivityInfo(playerId, entity);
			// 打点
			this.getDataGeter().logLuckyDiscountDraw(playerId, costFreeTimes > 0 ? 1 : 2, poolId, drawPoolCfg.getDiscount());

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	void onProtocolActivityBuyReq(int protocolType, String playerId, int cfgId, int count) {
		try {
			Optional<LuckyDiscountEntity> opDataEntity = getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.LUCKY_DISCOUNT_NOT_OPEN_VALUE);
				return;
			}
			LuckyDiscountEntity entity = opDataEntity.get();

			if (HawkTime.getMillisecond() > entity.getDeadline()) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.LUCKY_DISCOUNT_POOL_CLOSE_VALUE);
				return;
			}
			// 验证奖池有效
			List<LuckyDiscountShopCfg> shopGoodsCfg = LuckyDiscountShopCfg.getShopsByGroup(entity.getPoolId());
			if (null == shopGoodsCfg) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.LUCKY_DISCOUNT_POOL_CFG_VALUE);
				return;
			}
			
			LuckyDiscountPoolCfg poolCfg = HawkConfigManager.getInstance().getConfigByKey(LuckyDiscountPoolCfg.class, entity.getPoolId());
			if(null == poolCfg){
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.LUCKY_DISCOUNT_POOL_CFG_VALUE);
				return;			
			}
			
			// 验证道具合法
			List<LuckyDiscountShopCfg> findCfgs = shopGoodsCfg.stream().filter(e -> e.getId() == cfgId)
					.collect(Collectors.toList());
			if (null == findCfgs || 1 != findCfgs.size()) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.LUCKY_DISCOUNT_BUY_ITEM_CFG_VALUE);
				return;
			}
			// 验证道具限购次数
			LuckyDiscountShopCfg shopItemCfg = findCfgs.get(0);

			int boughtTimes = entity.getBuyTimes(cfgId);
			if (boughtTimes + count > shopItemCfg.getLimitNum()) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.LUCKY_DISCOUNT_BUY_TIMES_VALUE);
				return;
			}
			// 验证道具足够
			// 判断道具足够否
			boolean flag = this.getDataGeter().cost(playerId, shopItemCfg.getPriceList(), count,
					Action.LUCKY_DISCOUNT_BUY_COST, false);
			if (!flag) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocolType,
						Status.Error.LUCKY_DISCOUNT_BUY_GOLDEN_VALUE);
				return;
			}
			// 设置entity数据
			entity.addBuyTimes(cfgId, count);
			entity.notifyUpdate();
			// 发道具
			this.getDataGeter().takeReward(playerId, shopItemCfg.getItemList(), count, Action.LUCKY_DISCOUNT_BUY_GAIN, true, RewardOrginType.ACTIVITY_REWARD);
			// 操作成功
			PlayerPushHelper.getInstance().responseSuccess(playerId, protocolType);
			this.syncActivityInfo(playerId, entity);
			
			// 打点
			this.getDataGeter().logLuckyDiscountBuy(playerId, shopItemCfg.getItem(), shopItemCfg.getNewPrice(), count, poolCfg.getDiscount(), shopItemCfg.getId());
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}

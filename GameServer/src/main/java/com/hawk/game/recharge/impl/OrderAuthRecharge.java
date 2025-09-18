package com.hawk.game.recharge.impl;

import java.util.Optional;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.OrderAuthBuyEvent;
import com.hawk.activity.type.impl.order.OrderActivity;
import com.hawk.activity.type.impl.order.activityOrderTwo.OrderTwoActivity;
import com.hawk.activity.type.impl.order.activityOrderTwo.cfg.OrderTwoAuthorityCfg;
import com.hawk.activity.type.impl.order.activityOrderTwo.entity.OrderTwoEntity;
import com.hawk.activity.type.impl.order.entity.ActivityOrderEntity;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;

/**
 * 战令活动进阶礼包
 * 
 * @author lating
 *
 */
public class OrderAuthRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		return orderAuthBuyCheck(player) || orderTwoAuthBuyCheck(player, Integer.valueOf(giftCfg.getId()));
	}

	@Override
	public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
		ActivityManager.getInstance().postEvent(new OrderAuthBuyEvent(player.getId(), giftCfg.getId()));
		return true;
	}

	@Override
	public int getGiftType() {
		return RechargeType.ORDER_AUTH;
	}

	/**
	 * 购买战令权限礼包校验
	 * @return
	 */
	private boolean orderAuthBuyCheck(Player player) {
		Optional<OrderActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.ORDER_ACTIVITY_VALUE);
		if (!opActivity.isPresent()) {
			return false;
		}
		OrderActivity activity = opActivity.get();
		if (!activity.isOpening(player.getId())) {
			return false;
		}
		Optional<ActivityOrderEntity> opDataEntity = opActivity.get().getPlayerDataEntity(player.getId());
		if (!opDataEntity.isPresent()) {
			return false;
		}
		if (opDataEntity.get().getAuthorityId() != 0) {
			return false;
		}
		return true;
	}
	
	/**
	 * 购买战令权限礼包校验
	 * @return
	 */
	private boolean orderTwoAuthBuyCheck(Player player, int giftId) {
		Optional<OrderTwoActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.ORDER_TWO_VALUE);
		if (!opActivity.isPresent()) {
			return false;
		}
		OrderTwoActivity activity = opActivity.get();
		if (!activity.isOpening(player.getId())) {
			return false;
		}
		Optional<OrderTwoEntity> opDataEntity = opActivity.get().getPlayerDataEntity(player.getId());
		if (!opDataEntity.isPresent()) {
			return false;
		}
		int currAuth = opDataEntity.get().getAuthorityId();
		// 未购买过不受限制
		if (currAuth == 0) {
			return true;
		}

		OrderTwoAuthorityCfg currCfg = HawkConfigManager.getInstance().getConfigByKey(OrderTwoAuthorityCfg.class, currAuth);
		// 只有购买的是低阶战令的,才可以进行差价直购
		if (currCfg.getOrder() != 1) {
			return false;
		}
		
		OrderTwoAuthorityCfg cfg = HawkConfigManager.getInstance().getConfigByKey(OrderTwoAuthorityCfg.class, giftId);
		// 符合条件的,只能进行差价直购
		if (cfg == null || !cfg.isSupply()) {
			return false;
		}
		return true;
	}
}

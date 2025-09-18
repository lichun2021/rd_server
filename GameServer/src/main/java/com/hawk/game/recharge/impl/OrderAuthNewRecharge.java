package com.hawk.game.recharge.impl;

import java.util.Optional;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.impl.order.activityNewOrder.NewOrderActivity;
import com.hawk.activity.type.impl.order.activityNewOrder.entity.NewActivityOrderEntity;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;

/**
 * 新服战令礼包
 * 
 * @author lating
 *
 */
public class OrderAuthNewRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		Optional<NewOrderActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.NEW_ORDER_VALUE);
		if (!opActivity.isPresent()) {
			return false;
		}
		NewOrderActivity activity = opActivity.get();
		if (!activity.isOpening(player.getId())) {
			return false;
		}
		Optional<NewActivityOrderEntity> opDataEntity = activity.getPlayerDataEntity(player.getId());
		if (!opDataEntity.isPresent()) {
			return false;
		}
		if (opDataEntity.get().getAuthorityId() != 0) {
			return false;
		}
		return true;
	}

	@Override
	public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
		return true;
	}

	@Override
	public int getGiftType() {
		return RechargeType.NEW_ORDER;
	}

}

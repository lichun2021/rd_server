package com.hawk.game.recharge.impl;

import java.util.Optional;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.impl.order.activityEquipOrder.OrderEquipActivity;
import com.hawk.activity.type.impl.order.activityEquipOrder.cfg.OrderEquipAuthorityCfg;
import com.hawk.activity.type.impl.order.activityEquipOrder.entity.OrderEquipEntity;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;

/**
 * 装备战令进阶礼包
 * 
 * @author lating
 *
 */
public class OrderEquipAuthGiftRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		Optional<OrderEquipActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.ORDER_EQUIP_VALUE);
		if (!opActivity.isPresent()) {
			return false;
		}
		
		int giftId = Integer.parseInt(giftCfg.getId());
		OrderEquipAuthorityCfg cfg = HawkConfigManager.getInstance().getConfigByKey(OrderEquipAuthorityCfg.class, giftId);
		if (cfg == null) {
			return false;
		}
		OrderEquipActivity activity = opActivity.get();
		if (!activity.isOpening(player.getId())) {
			return false;
		}
		Optional<OrderEquipEntity> opDataEntity = opActivity.get().getPlayerDataEntity(player.getId());
		if (!opDataEntity.isPresent()) {
			return false;
		}
		int currAuth = opDataEntity.get().getAuthorityId();
		if (currAuth > 0) {
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
		return RechargeType.ORDER_EQUIP_AUTH;
	}

}

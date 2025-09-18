package com.hawk.game.recharge.impl;

import java.util.Optional;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.impl.armamentexchange.ArmamentExchangeActivity;
import com.hawk.activity.type.impl.armamentexchange.entity.ArmamentExchangeEntity;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;

/**
 * 军备礼包
 * 
 * @author lating
 *
 */
public class ArmamentExchangeGiftRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		Optional<ArmamentExchangeActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.ARMAMENT_EXCHANGE_VALUE);
		if (!opActivity.isPresent()) {
			return false;
		}
		ArmamentExchangeActivity activity = opActivity.get();
		if (!activity.isOpening(player.getId())) {
			return false;
		}
		Optional<ArmamentExchangeEntity> opDataEntity = activity.getPlayerDataEntity(player.getId());
		if (!opDataEntity.isPresent()) {
			return false;
		}
		if (opDataEntity.get().getIsOpen() != 0) {
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
		return RechargeType.ARMAMENT_EXCHANGE;
	}

}

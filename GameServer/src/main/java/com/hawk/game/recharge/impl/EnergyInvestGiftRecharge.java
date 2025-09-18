package com.hawk.game.recharge.impl;

import java.util.Optional;

import org.hawk.log.HawkLog;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.EnergyInvestGiftBuyEvent;
import com.hawk.activity.type.impl.energyInvest.EnergyInvestActivity;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;

/**
 * 
 * 能量源投资礼包
 * 
 * @author lating
 *
 */
public class EnergyInvestGiftRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.ENERGY_INVEST_VALUE);
		if (!opActivity.isPresent()) {
			player.sendError(protocol, Status.Error.ACTIVITY_NOT_OPEN_VALUE, 0);
			HawkLog.errPrintln("EnergyInvestActivity not exist, playerId: {}", player.getId());
			return false;
		}
		
		EnergyInvestActivity activity = (EnergyInvestActivity)opActivity.get();
		if (!activity.isOpening(player.getId())) {
			player.sendError(protocol, Status.Error.ACTIVITY_NOT_OPEN_VALUE, 0);
			HawkLog.errPrintln("EnergyInvestActivity not opening, playerId: {}", player.getId());
			return false;
		}
		
		if (!activity.canPayforGift(player.getId(), giftCfg.getId())) {
			player.sendError(protocol, Status.Error.PAY_GOODS_CANNOT_SALE, 0);
			HawkLog.errPrintln("EnergyInvestActivity reward config error, playerId: {}", player.getId());
			return false;
		}
		
		return true;
	}

	@Override
	public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
		ActivityManager.getInstance().postEvent(new EnergyInvestGiftBuyEvent(player.getId(), giftCfg.getId()));
		return true;
	}

	@Override
	public int getGiftType() {
		return RechargeType.ENERGY_INVEST;
	}

}

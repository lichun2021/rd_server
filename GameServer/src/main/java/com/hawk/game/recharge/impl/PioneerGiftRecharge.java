package com.hawk.game.recharge.impl;

import java.util.Optional;

import org.hawk.log.HawkLog;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.PioneerGiftPurchaseEvent;
import com.hawk.activity.type.impl.pioneergift.PioneerGiftActivity;
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
 * 先锋豪礼礼包
 * 
 * @author lating
 *
 */
public class PioneerGiftRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.PIONEER_GIFTS_VALUE);
		if (!opActivity.isPresent()) {
			player.sendError(protocol, Status.Error.PIONEER_ACTIVITY_NOT_OPEN_VALUE, 0);
			HawkLog.errPrintln("PioneerGiftActivity not exist, playerId: {}", player.getId());
			return false;
		}
		
		PioneerGiftActivity activity = (PioneerGiftActivity)opActivity.get();
		int result = activity.selectGiftCheck(player.getId(), giftCfg.getPioneerGiftType());
		if (result > 0) {
			player.sendError(protocol, result, 0);
			HawkLog.errPrintln("PioneerGiftActivity check gift failed, playerId: {}, type: {}, result: {}", player.getId(), giftCfg.getPioneerGiftType(), result);
			return false;
		}
		
		return true;
	}

	@Override
	public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
		ActivityManager.getInstance().postEvent(new PioneerGiftPurchaseEvent(player.getId(), giftCfg.getPioneerGiftType()));
		return true;
	}

	@Override
	public int getGiftType() {
		return RechargeType.PIONEER_GIFT;
	}

}

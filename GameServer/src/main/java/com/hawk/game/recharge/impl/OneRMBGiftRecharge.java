package com.hawk.game.recharge.impl;

import java.util.Optional;

import org.hawk.log.HawkLog;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.OneRMBPurchaseEvent;
import com.hawk.activity.type.impl.onermbpurchase.OneRMBPurchaseActivity;
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
 * 一元购礼包
 * 
 * @author lating
 *
 */
public class OneRMBGiftRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.ONE_RMB_PURCHASE_VALUE);
		if (!opActivity.isPresent()) {
			player.sendError(protocol, Status.Error.ONERMB_PURCHASE_NOT_OPEN_VALUE, 0);  
			HawkLog.errPrintln("OneRMBPurchaseActivity not exist, playerId: {}, openId: {}", player.getId(), player.getOpenId());
			return false;
		}
		
		OneRMBPurchaseActivity activity = (OneRMBPurchaseActivity)opActivity.get();
		if (!activity.isOpening(player.getId())) {
			player.sendError(protocol, Status.Error.ONERMB_PURCHASE_NOT_OPEN_VALUE, 0); 
			HawkLog.errPrintln("OneRMBPurchaseActivity not open, playerId: {}, openId: {}", player.getId(), player.getOpenId());
			return false;
		}
		
		return true;
	}

	@Override
	public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
		ActivityManager.getInstance().postEvent(new OneRMBPurchaseEvent(player.getId()));
		return true;
	}

	@Override
	public int getGiftType() {
		return RechargeType.ONE_RMB_PURCHASE;
	}

}

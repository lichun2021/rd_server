package com.hawk.game.recharge.impl;

import java.util.Optional;
import org.hawk.log.HawkLog;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.activity.type.impl.alliesWishing.AllianceWishActivity;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;

/**
 * 盟军祝福直购礼包
 * 
 * @author lating
 *
 */
public class AllianceWishGiftRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.ALLIANCE_WISH_VALUE);
		if (!opActivity.isPresent()) {
			player.sendError(protocol, Status.Error.ACTIVITY_NOT_OPEN_VALUE, 0);
			return false;
		}
		AllianceWishActivity activity =  (AllianceWishActivity)opActivity.get();
		if (!activity.isOpening(player.getId())) {
			player.sendError(protocol, Status.Error.ACTIVITY_NOT_OPEN_VALUE, 0);
			return false;
		}
		if(activity == null || !activity.buyGiftCheck(player.getId())){
			player.sendError(protocol, Status.Error.GOODS_CAN_NOT_SELL_VALUE, 0);
			HawkLog.errPrintln("GreatGiftActivity player buy can not sell item, playerId: {}, openId: {}, giftId: {}", player.getId(), player.getOpenId(), giftCfg.getId());
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
		return RechargeType.ALLIANCE_WISH_GIFT;
	}

}

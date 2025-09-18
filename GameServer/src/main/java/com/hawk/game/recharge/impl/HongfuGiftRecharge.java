package com.hawk.game.recharge.impl;

import java.util.Optional;
import org.hawk.log.HawkLog;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.HongFuGiftBuyEvent;
import com.hawk.activity.type.impl.hongfugift.HongFuGiftActivity;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;

/**
 * 
 * 洪福礼包
 * 
 * @author lating
 *
 */
public class HongfuGiftRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		Optional<HongFuGiftActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.HONG_FU_GIFT_VALUE);
		if (!opActivity.isPresent()) {
			player.sendError(protocol, Status.Error.ACTIVITY_NOT_OPEN_VALUE, 0);
			HawkLog.errPrintln("PlayerRechargeModule HongFuGiftBuyCheck activity no open, playerId: {}, giftId:{}", player.getId(), giftCfg.getId());
			return false;
		}
		
		HongFuGiftActivity activity = opActivity.get();
		int result = activity.checkBuySuccess(player.getId(), giftCfg.getId());
		if (result > 0) {
			player.sendError(protocol, result, 0);
			HawkLog.errPrintln("PlayerRechargeModule HongFuGiftBuyCheck check gift failed, playerId: {}, type: {}, result: {}", player.getId(), giftCfg.getId(), result);
			return false;
		}
		return true;
	}

	@Override
	public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
		ActivityManager.getInstance().postEvent(new HongFuGiftBuyEvent(player.getId(), giftCfg.getId()));
		return true;
	}

	@Override
	public int getGiftType() {
		return RechargeType.HONG_FU_GIFT;
	}

}

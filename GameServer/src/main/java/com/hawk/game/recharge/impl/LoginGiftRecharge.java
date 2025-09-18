package com.hawk.game.recharge.impl;

import java.util.Optional;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.activity.type.impl.logingift.LoginGiftActivity;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;

/**
 * 新版新手登录进阶礼
 * 
 * @author lating
 *
 */
public class LoginGiftRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.LOGIN_GIFT_VALUE);
		if (!opActivity.isPresent()) {
			player.sendError(protocol, Status.Error.ACTIVITY_NOT_OPEN_VALUE, 0);
			return false;
		}
		
		LoginGiftActivity activity = (LoginGiftActivity)opActivity.get();
		int result = activity.checkAdvanceBuy(player.getId(), giftCfg.getId());
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			player.sendError(protocol, result, 0);
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
		return RechargeType.LOGIN_GIFTS;
	}

}

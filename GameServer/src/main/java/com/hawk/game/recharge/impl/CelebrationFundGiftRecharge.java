package com.hawk.game.recharge.impl;

import java.util.Optional;

import org.hawk.log.HawkLog;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.CelebrationFundGiftEvent;
import com.hawk.activity.type.impl.celebrationFund.CelebrationFundActivity;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;

/**
 * 
 * 周年庆庆典基金礼包
 * 
 * @author LiJialiang,FangWeijie
 *
 */
public class CelebrationFundGiftRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		Optional<CelebrationFundActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.CELEBRATION_FUND_VALUE);
		if (!opActivity.isPresent()) {
			HawkLog.errPrintln("celebration fund buy check failed, activity optional empty, playerId: {}, giftId: {}", player.getId(), giftCfg.getId());
			return false;
		}

		CelebrationFundActivity activity = opActivity.get();
		int result = activity.buyGiftCheck(player.getId(), giftCfg.getId());
		if (result != 0) {
			player.sendError(protocol, result, 0);
			return false;
		}
		return true;
	}

	@Override
	public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
		ActivityManager.getInstance().postEvent(new CelebrationFundGiftEvent(player.getId(), giftCfg.getId()));
		return true;
	}

	@Override
	public int getGiftType() {
		return RechargeType.CELEBRAION_FUND_ACTIVITY;
	}

}

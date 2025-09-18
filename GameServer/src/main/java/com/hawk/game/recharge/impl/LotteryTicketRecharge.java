package com.hawk.game.recharge.impl;

import java.util.Optional;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.impl.lotteryTicket.LotteryTicketActivity;
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
public class LotteryTicketRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		Optional<LotteryTicketActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.LOTTERY_TICKET_VALUE);
		if (!opActivity.isPresent()) {
			return false;
		}
		LotteryTicketActivity activity = opActivity.get();
		if (!activity.isOpening(player.getId())) {
			return false;
		}
		return activity.buyGiftVerify(player.getId(), giftCfg.getId());
	}

	@Override
	public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
		return true;
	}

	@Override
	public int getGiftType() {
		return RechargeType.LOTTERY_TICKET;
	}

}

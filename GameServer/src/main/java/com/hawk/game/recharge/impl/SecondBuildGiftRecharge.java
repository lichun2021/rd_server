package com.hawk.game.recharge.impl;

import org.hawk.log.HawkLog;

import com.hawk.game.GsApp;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.msg.SecondBuildGiftBuyMsg;
import com.hawk.game.msg.SuperGiftDirectBuyMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;

/**
 * 
 * 第二城建队列礼包
 * 
 * @author lating
 *
 */
public class SecondBuildGiftRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		boolean unlock = player.getData().isSecondBuildUnlock();
		if (unlock) {
			HawkLog.errPrintln("player second build queue has unlocked, playerId: {}", player.getId());
			player.sendError(protocol,  Status.Error.SECOND_BUILD_GIFT_BUY_LIMIT, 0);
			return false;
		}
		return true;
	}

	@Override
	public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
		GsApp.getInstance().postMsg(player, new SecondBuildGiftBuyMsg());
		GsApp.getInstance().postMsg(player, new SuperGiftDirectBuyMsg(giftCfg.getId()));
		return true;
	}

	@Override
	public int getGiftType() {
		return RechargeType.SECOND_BUILD_GIFT;
	}

}

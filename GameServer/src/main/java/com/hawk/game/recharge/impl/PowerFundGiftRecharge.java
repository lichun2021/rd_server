package com.hawk.game.recharge.impl;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.PowerFundBuyEvent;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;

/**
 * 
 * 战力基金礼包
 * 
 * @author lating
 *
 */
public class PowerFundGiftRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		return true;
	}

	@Override
	public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
		ActivityManager.getInstance().postEvent(new PowerFundBuyEvent(player.getId(), giftCfg.getId()));
		return true;
	}

	@Override
	public int getGiftType() {
		return RechargeType.POWER_FUND_GIFT;
	}

}

package com.hawk.game.recharge.impl;

import com.hawk.game.GsApp;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.module.travelshop.TravelShopFriendly;
import com.hawk.game.msg.TravelShopFriendlyCardBuyMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;

/**
 * 
 * 黑市商店友好度特权卡
 * 
 * @author lating
 *
 */
public class TravelShopGiftRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		TravelShopFriendly friendlyInfo = RedisProxy.getInstance().getTravelShopFriendlyInfo(player.getId());
		// 当前特权卡还在持续时间内
		if (friendlyInfo.privilegeEffect()) {
			return false;
		}
		return true;
	}

	@Override
	public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
		GsApp.getInstance().postMsg(player, new TravelShopFriendlyCardBuyMsg());
		return true;
	}

	@Override
	public int getGiftType() {
		return RechargeType.TRAVEL_SHOP_FRIEND_CARD;
	}

}

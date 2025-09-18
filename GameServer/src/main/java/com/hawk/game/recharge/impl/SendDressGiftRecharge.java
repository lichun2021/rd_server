package com.hawk.game.recharge.impl;

import java.util.Map;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;

/**
 * 
 * 装扮赠送信使礼包
 * 
 * @author lating
 *
 */
public class SendDressGiftRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		Map<Integer, Integer> sendDressGiftInfo = RedisProxy.getInstance().getSendDressGiftInfo(player.getId());
		// 每周限次
		int alreadyBuy = sendDressGiftInfo.getOrDefault(giftCfg.getId(), 0);
		if (alreadyBuy >= giftCfg.getPayCount()) {
			return false;
		}
		return true;
	}

	@Override
	public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
		RedisProxy.getInstance().updateSendDressGiftInfo(player.getId(), Integer.parseInt(giftCfg.getId()));
		player.getPush().syncSendDressGiftInfo();
		return true;
	}

	@Override
	public int getGiftType() {
		return RechargeType.SEND_DRESS;
	}
	
}

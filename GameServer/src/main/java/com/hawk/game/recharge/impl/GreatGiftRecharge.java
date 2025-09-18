package com.hawk.game.recharge.impl;

import java.util.Optional;
import java.util.Set;

import org.hawk.log.HawkLog;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.GreatGiftBuyEvent;
import com.hawk.activity.type.impl.greatGift.GreatGiftActivity;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;

/**
 * 
 * 超值好礼礼包
 * 
 * @author lating
 *
 */
public class GreatGiftRecharge extends AbstractGiftRecharge {
	
	@Override
	public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
		if (!giftCfg.isGreatGift()) {
			return false;
		}
		
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.GREAT_GIFT_VALUE);
		GreatGiftActivity activity = null;
		if (opActivity.isPresent()) {
			activity = (GreatGiftActivity)opActivity.get();
		}
		
		if(activity == null || !activity.canBuy(player.getId(), giftCfg.getId())){
			player.sendError(protocol, Status.Error.GOODS_CAN_NOT_SELL_VALUE, 0);
			HawkLog.errPrintln("GreatGiftActivity player buy can not sell item, playerId: {}, openId: {}, giftId: {}", player.getId(), player.getOpenId(), giftCfg.getId());
			return false;
		}
		
		Set<String> goodsIds = RedisProxy.getInstance().getRechargeOutterGoodsId(player.getId());
		if (goodsIds.contains(giftCfg.getId())) {
			player.sendError(protocol, Status.Error.PAY_GIFT_LAST_UNFINISH, 0);
			HawkLog.errPrintln("unfinished recharge outter, playerId: {}, giftId: {}",  player.getId(), giftCfg.getId());
			return false;
		}
		
		return true;
	}

	@Override
	public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
		ActivityManager.getInstance().postEvent(new GreatGiftBuyEvent(player.getId(), giftCfg.getId()));
		return true;
	}

	@Override
	public int getGiftType() {
		return RechargeType.GREAT_GIFT;
	}

}

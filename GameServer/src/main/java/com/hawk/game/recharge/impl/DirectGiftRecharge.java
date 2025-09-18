package com.hawk.game.recharge.impl;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.BuyDirectGiftEvent;
import com.hawk.activity.type.impl.directGift.DirectGiftActivity;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Recharge;
import com.hawk.game.protocol.Status;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;

import java.util.Optional;
import java.util.Set;

import org.hawk.log.HawkLog;

public class DirectGiftRecharge extends AbstractGiftRecharge {
    @Override
    public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, Recharge.RechargeBuyItemRequest req, int protocol) {
        Optional<DirectGiftActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(Activity.ActivityType.DIRECT_GIFT_VALUE);
        if (!opActivity.isPresent()){
            return false;
        }
        
        DirectGiftActivity activity = opActivity.get();
        if (!activity.buyGiftCheck(player.getId(),giftCfg.getId())){
        	player.sendError(protocol, Status.Error.GOODS_CAN_NOT_SELL_VALUE, 0);
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
        ActivityManager.getInstance().postEvent(new BuyDirectGiftEvent(player.getId(), giftCfg.getId()));
        return true;
    }

    @Override
    public int getGiftType() {
        return RechargeType.DIRECT_GIFT_ACTIVITY;
    }
}

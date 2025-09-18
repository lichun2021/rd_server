package com.hawk.game.recharge.impl;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.DevelopFastBuyEvent;
import com.hawk.activity.type.impl.developFast.DevelopFastActivity;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Recharge;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;

import java.util.Optional;

public class DevelopFastActivityRecharge extends AbstractGiftRecharge {
    @Override
    public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, Recharge.RechargeBuyItemRequest req, int protocol) {
        return buyCheck(player, giftCfg.getId());
    }

    @Override
    public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
        ActivityManager.getInstance().postEvent(new DevelopFastBuyEvent(player.getId(), giftCfg.getId()));
        return true;
    }

    @Override
    public int getGiftType() {
        return RechargeType.DEVELOP_FAST_ACTIVITY;
    }

    private boolean buyCheck(Player player, String payGifrId){
        Optional<DevelopFastActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(Activity.ActivityType.DEVELOP_FAST_VALUE);
        if (!opActivity.isPresent()) {
            return false;
        }
        DevelopFastActivity activity = opActivity.get();
        //进行购买校验
        if(!activity.checkAuthBuy(player.getId(), payGifrId)){
            return false;
        }
        return true;
    }
}

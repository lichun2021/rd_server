package com.hawk.game.recharge.impl;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.StarLightSignBuyEvent;
import com.hawk.activity.type.impl.starLightSign.StarLightSignActivity;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Recharge;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;

import java.util.Optional;

public class StarLightSignActivityRecharge extends AbstractGiftRecharge {
    @Override
    public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, Recharge.RechargeBuyItemRequest req, int protocol) {
        return buyCheck(player, giftCfg.getId());
    }

    @Override
    public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
        ActivityManager.getInstance().postEvent(new StarLightSignBuyEvent(player.getId(), giftCfg.getId()));
        return true;
    }

    @Override
    public int getGiftType() {
        return RechargeType.STAR_LIGHT_SIGN_ACTIVITY;
    }

    private boolean buyCheck(Player player, String payGifrId){
        Optional<StarLightSignActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(Activity.ActivityType.WORLD_HONOR_ACTIVITY_VALUE);
        if (!opActivity.isPresent()) {
            return false;
        }
        StarLightSignActivity activity = opActivity.get();
        //进行购买校验
        if(!activity.checkAuthBuy(player.getId(), payGifrId)){
            return false;
        }
        return true;
    }
}

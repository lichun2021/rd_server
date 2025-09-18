package com.hawk.game.recharge.impl;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.DiffInfoSaveBuyEvent;
import com.hawk.activity.type.impl.diffInfoSave.DiffInfoSaveActivity;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Recharge;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;

import java.util.Optional;

public class DiffInfoSaveActivityRecharge extends AbstractGiftRecharge {
    @Override
    public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, Recharge.RechargeBuyItemRequest req, int protocol) {
        return buyCheck(player, giftCfg.getId());
    }

    @Override
    public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
        ActivityManager.getInstance().postEvent(new DiffInfoSaveBuyEvent(player.getId(), giftCfg.getId()));
        return true;
    }

    @Override
    public int getGiftType() {
        return RechargeType.DIFF_INFO_SAVE_ACTIVITY;
    }

    private boolean buyCheck(Player player, String payGifrId){
        Optional<DiffInfoSaveActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(Activity.ActivityType.DIFF_INFO_SAVE_VALUE);
        if (!opActivity.isPresent()) {
            return false;
        }
        DiffInfoSaveActivity activity = opActivity.get();
        //进行购买校验
        if(!activity.checkAuthBuy(player.getId(), payGifrId)){
            return false;
        }
        return true;
    }
}

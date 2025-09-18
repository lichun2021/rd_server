package com.hawk.game.recharge.impl;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.impl.plantSoldierFactory.PlantSoldierFactoryActivity;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Recharge;
import com.hawk.game.protocol.Status;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;

import java.util.Optional;

public class PlantSoldierRecharge extends AbstractGiftRecharge {
    @Override
    public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, Recharge.RechargeBuyItemRequest req, int protocol) {
        Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.PLANT_SOLDIER_FACTORY_VALUE);
        if (!opActivity.isPresent()) {
            player.sendError(protocol, Status.Error.ACTIVITY_NOT_OPEN_VALUE, 0);
            return false;
        }
        PlantSoldierFactoryActivity activity = (PlantSoldierFactoryActivity) opActivity.get();
        int result = activity.buyItemCheck(player.getId(), giftCfg.getId());
        if (result != 0) {
            player.sendError(protocol, result, 0);
            return false;
        }

        return true;
    }

    @Override
    public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
        return true;
    }

    @Override
    public int getGiftType() {
        return RechargeType.PLANT_SOLDIER_FACTORY_ACTIVITY;
    }
}

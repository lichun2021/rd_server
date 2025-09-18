package com.hawk.game.recharge.impl;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.HomeLandPuzzleBuyEvent;
import com.hawk.activity.type.impl.homeland.HomeLandPuzzleActivity;
import com.hawk.activity.type.impl.homeland.cfg.HomeLandPuzzleShopCfg;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Recharge.RechargeBuyItemRequest;
import com.hawk.game.protocol.Status;
import com.hawk.game.recharge.AbstractGiftRecharge;
import com.hawk.game.recharge.RechargeType;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import java.util.Optional;

/**
 * 幸运星礼包
 *
 * @author zhy
 */
public class HomeLandPuzzleRecharge extends AbstractGiftRecharge {

    @Override
    public boolean detailGiftBuyCheck(Player player, PayGiftCfg giftCfg, RechargeBuyItemRequest req, int protocol) {
        Optional<HomeLandPuzzleActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(Activity.ActivityType.HOME_LAND_PUZZLE_VALUE);
        if (!opActivity.isPresent()) {
            player.sendError(protocol, Status.Error.ACTIVITY_NOT_OPEN_VALUE, 0);
            HawkLog.errPrintln("HomeLandPuzzle activity no open, playerId: {}, giftId:{}", player.getId(), giftCfg.getId());
            return false;
        }
        int giftId = HomeLandPuzzleShopCfg.getGiftId(giftCfg.getId());
        HomeLandPuzzleShopCfg shopCfg = HawkConfigManager.getInstance().getConfigByKey(HomeLandPuzzleShopCfg.class,
                giftId);
        if (shopCfg == null) {
            return false;
        }
        HomeLandPuzzleActivity activity = opActivity.get();
        int result = activity.canPayRMBGift(player.getId(), giftCfg.getId());
        if (result != 0) {
            player.sendError(protocol, result, 0);
            return false;
        }
        return true;
    }

    @Override
    public boolean deliverGoodsDetail(Player player, PayGiftCfg giftCfg, RechargeEntity rechargeEntity) {
        ActivityManager.getInstance().postEvent(new HomeLandPuzzleBuyEvent(player.getId(), giftCfg.getId()));
        return true;
    }

    @Override
    public int getGiftType() {
        return RechargeType.HOME_LAND_PUZZLE;
    }

}

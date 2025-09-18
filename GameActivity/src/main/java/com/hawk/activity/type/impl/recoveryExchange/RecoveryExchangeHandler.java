package com.hawk.activity.type.impl.recoveryExchange;

import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.*;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;

/**
 * 道具回收活动
 *
 * @author richard
 */
public class RecoveryExchangeHandler extends ActivityProtocolHandler {
    /**
     * 积分兑换物品勾选变更请求
     */
    @ProtocolHandler(code = HP.code2.ITEM_RECYCLE_TIP_REQ_VALUE)
    public void lottery(HawkProtocol hawkProtocol, String playerId) {
        RecoveryExchangeActivity activity = this.getActivity(ActivityType.RECOVERY_EXCHANGE_ACTIVITY);
        if (activity == null) {
            return;
        }
        PBItemRecycleTipsReq req = hawkProtocol.parseProtocol(
                PBItemRecycleTipsReq.getDefaultInstance());
        activity.updateActivityTips(playerId, req.getTipsList());
    }

    /**
     * 积分兑换物品请求
     */
    @ProtocolHandler(code = HP.code2.ITEM_RECYCLE_ENTEGRAL_EXCHANGE_ITEMS_REQ_VALUE)
    public void updateCare(HawkProtocol hawkProtocol, String playerId) {
        RecoveryExchangeActivity activity = this.getActivity(ActivityType.RECOVERY_EXCHANGE_ACTIVITY);
        if (activity == null) {
            return;
        }
        PBEntegralExchangeItemsReq req = hawkProtocol.parseProtocol(
                PBEntegralExchangeItemsReq.getDefaultInstance());

        activity.itemExchange(playerId, req.getExchangeItems().getExchangeId(),
                req.getExchangeItems().getCount());
    }

    /**
     * 物品兑换积分请求
     *
     * @param hawkProtocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code2.ITEM_RECYCLE_RECYCLE_REQ_VALUE)
    public void itemExchange(HawkProtocol hawkProtocol, String playerId) {
        RecoveryExchangeActivity activity = this.getActivity(ActivityType.RECOVERY_EXCHANGE_ACTIVITY);
        if (activity == null) {
            return;
        }
        PBItemRecycleReq req = hawkProtocol.parseProtocol(
                PBItemRecycleReq.getDefaultInstance());
        activity.onItemRecycleReq(playerId, req);
    }

    /**
     * 兑换积分的物品赎回请求
     */
    @ProtocolHandler(code = HP.code2.ITEM_RECYCLE_REDEMPTION_REQ_VALUE)
    public void redemptionReq(HawkProtocol hawkProtocol, String playerId) {
        RecoveryExchangeActivity activity = this.getActivity(ActivityType.RECOVERY_EXCHANGE_ACTIVITY);
        if (activity == null) {
            return;
        }
        PBRedemptionReq req = hawkProtocol.parseProtocol(
                PBRedemptionReq.getDefaultInstance());
        activity.onRedemptionItemReq(playerId, req);
    }

    /**
     * 物品精炼请求
     */
    @ProtocolHandler(code = HP.code2.ITEM_RECYCLE_RECOVERY_REQ_VALUE)
    public void itemRecoveryReq(HawkProtocol hawkProtocol, String playerId) {
        RecoveryExchangeActivity activity = this.getActivity(ActivityType.RECOVERY_EXCHANGE_ACTIVITY);
        if (activity == null) {
            return;
        }
        PBRecoveryReq req = hawkProtocol.parseProtocol(
                PBRecoveryReq.getDefaultInstance());
        activity.onItemRecovery(playerId, req);
    }
}
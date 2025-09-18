package com.hawk.activity.type.impl.deepTreasure;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.PBDeepTreasureOpenboxItemBuyReq;
import com.hawk.game.protocol.Activity.PBDeepTreasureOpenboxReq;
import com.hawk.game.protocol.Activity.PBPBAllianceWishExchangeReq;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

public class DeepTreasureHandler extends ActivityProtocolHandler {

    /**
     * 开箱子
     */
    @ProtocolHandler(code = HP.code2.DEEP_TREASURE_OPEN_BOX_REQ_VALUE)
    public void medalTreasureLottery(HawkProtocol protocol, String playerId) {
        DeepTreasureActivity activity = getActivity(ActivityType.DEEP_TREASURE_ACTIVITY);
        PBDeepTreasureOpenboxReq req = protocol.parseProtocol(PBDeepTreasureOpenboxReq.getDefaultInstance());
        Result<?> result = activity.lottery(playerId, req);
        if (result.isFail()) {
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
        activity.responseSuccess(playerId, protocol.getType());
    }

    /**
     * 刷新
     */
    @ProtocolHandler(code = HP.code2.DEEP_TREASURE_REFRESH_BOX_REQ_VALUE)
    public void refreshNineBox(HawkProtocol protocol, String playerId) {
        DeepTreasureActivity activity = getActivity(ActivityType.DEEP_TREASURE_ACTIVITY);
        Result<?> result = activity.refreshNineBox(playerId, false);
        if (result.isFail()) {
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
        activity.syncActivityDataInfo(playerId);
        activity.responseSuccess(playerId, protocol.getType());
    }

    /**
     * 买开箱子道具
     */
    @ProtocolHandler(code = HP.code2.DEEP_TREASURE_BOX_ITEM_BUY_REQ_VALUE)
    public void purchaseItemCost(HawkProtocol protocol, String playerId) {
        PBDeepTreasureOpenboxItemBuyReq req = protocol.parseProtocol(PBDeepTreasureOpenboxItemBuyReq.getDefaultInstance());
        DeepTreasureActivity activity = getActivity(ActivityType.DEEP_TREASURE_ACTIVITY);
        Result<?> result = activity.purchaseItemCost(playerId, req.getNumber());
        if (result.isFail()) {
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
        activity.responseSuccess(playerId, protocol.getType());
    }

    @ProtocolHandler(code = HP.code2.DEEP_TREASURE_ITEM_EXECHANGE_REQ_VALUE)
    public void allianceGuildExchange(HawkProtocol hawkProtocol, String playerId) {
        DeepTreasureActivity activity = this.getActivity(ActivityType.DEEP_TREASURE_ACTIVITY);
        if (activity == null) {
            return;
        }
        PBPBAllianceWishExchangeReq req = hawkProtocol.parseProtocol(PBPBAllianceWishExchangeReq.getDefaultInstance());
        activity.itemExchange(playerId, req.getId(), req.getNum(), hawkProtocol.getType());
        activity.responseSuccess(playerId, hawkProtocol.getType());
    }

}

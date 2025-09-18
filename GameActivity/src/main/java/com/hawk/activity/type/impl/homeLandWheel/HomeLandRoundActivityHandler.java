package com.hawk.activity.type.impl.homeLandWheel;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.ActivityHomeLandRound;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

public class HomeLandRoundActivityHandler extends ActivityProtocolHandler {
    @ProtocolHandler(code = HP.code2.HOME_ROUND_ACT_INFO_C_VALUE)
    public void onActivityInfoReq(HawkProtocol protocol, String playerId) {
        HomeLandRoundActivity activity = getActivity(ActivityType.HOME_LAND_ROUND);
        if (activity != null && activity.isOpening(playerId)) {
            activity.syncActivityDataInfo(playerId);
        }
    }

    /**
     * 抽奖
     *
     * @param protocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code2.HOME_ROUND_DRAW_C_VALUE)
    public void onDrawReq(HawkProtocol protocol, String playerId) {
        ActivityHomeLandRound.HomeLandRoundDrawReq req = protocol.parseProtocol(ActivityHomeLandRound.HomeLandRoundDrawReq.getDefaultInstance());
        HomeLandRoundActivity activity = getActivity(ActivityType.HOME_LAND_ROUND);
        Result<?> result = activity.onDraw(playerId, req.getDrawType());
        if (result.isFail()) {
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    /**
     * 商店兑换
     *
     * @param protocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code2.HOME_ROUND_SHOP_EXCHANGE_C_VALUE)
    public void onShopExchangeReq(HawkProtocol protocol, String playerId) {
        ActivityHomeLandRound.HomeLandRoundExchangeReq req = protocol.parseProtocol(ActivityHomeLandRound.HomeLandRoundExchangeReq.getDefaultInstance());
        HomeLandRoundActivity activity = getActivity(ActivityType.HOME_LAND_ROUND);
        Result<?> result = activity.onShopExchange(playerId, req.getCfgId(), req.getCount());
        if (result.isFail()) {
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    /**
     * 领取当前层数奖励
     *
     * @param protocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code2.HOME_ROUND_TAKE_REWARD_C_VALUE)
    public void onTakeRewardReq(HawkProtocol protocol, String playerId) {
        HomeLandRoundActivity activity = getActivity(ActivityType.HOME_LAND_ROUND);
        Result<?> result = activity.onTakeFloorReward(playerId);
        if (result.isFail()) {
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    /**
     * 购买道具
     *
     * @param protocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code2.HOME_ROUND_BUY_C_VALUE)
    public void onBuyReq(HawkProtocol protocol, String playerId) {
        ActivityHomeLandRound.HomeLandRoundBuyReq req = protocol.parseProtocol(ActivityHomeLandRound.HomeLandRoundBuyReq.getDefaultInstance());
        HomeLandRoundActivity activity = getActivity(ActivityType.HOME_LAND_ROUND);
        Result<?> result = activity.onBuy(playerId, req.getCount());
        if (result.isFail()) {
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }
}

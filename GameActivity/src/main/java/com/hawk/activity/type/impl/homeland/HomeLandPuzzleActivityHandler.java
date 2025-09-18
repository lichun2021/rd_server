package com.hawk.activity.type.impl.homeland;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.ActivityHomeLandPuzzle;
import com.hawk.game.protocol.ActivityHomeLandPuzzle.HomeLandPuzzleScratchReq;
import com.hawk.game.protocol.ActivityHomeLandPuzzle.HomeLandPuzzleShopBuyReq;
import com.hawk.game.protocol.ActivityHomeLandPuzzle.HomeLandPuzzleShopExchangeReq;
import com.hawk.game.protocol.ActivityHomeLandPuzzle.HomeLandPuzzleTipsInfoReq;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import java.util.ArrayList;
import java.util.List;

public class HomeLandPuzzleActivityHandler extends ActivityProtocolHandler {
    /**
     * 请求活动信息
     *
     * @param protocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code2.HOME_PUZZLE_ACT_INFO_C_VALUE)
    public void onActivityInfoReq(HawkProtocol protocol, String playerId) {
        HomeLandPuzzleActivity activity = getActivity(ActivityType.HOME_LAND_PUZZLE);
        if (activity != null && activity.isOpening(playerId)) {
            activity.syncActivityDataInfo(playerId);
        }
    }

    /**
     * 请求活动信息
     *
     * @param protocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code2.HOME_PUZZLE_SCRATCH_C_VALUE)
    public void onScratchReq(HawkProtocol protocol, String playerId) {
        HomeLandPuzzleScratchReq req = protocol.parseProtocol(HomeLandPuzzleScratchReq.getDefaultInstance());
        HomeLandPuzzleActivity activity = getActivity(ActivityType.HOME_LAND_PUZZLE);
        Result<?> result = activity.onScratch(playerId, req.getCount());
        if (result.isFail()) {
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    /**
     * 商店购买
     *
     * @param protocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code2.HOME_PUZZLE_SHOP_BUY_C_VALUE)
    public void onShopBuyReq(HawkProtocol protocol, String playerId) {
        HomeLandPuzzleShopBuyReq req = protocol.parseProtocol(HomeLandPuzzleShopBuyReq.getDefaultInstance());
        HomeLandPuzzleActivity activity = getActivity(ActivityType.HOME_LAND_PUZZLE);
        Result<?> result = activity.onShopBuy(playerId, req.getCfgId(), req.getCount());
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
    @ProtocolHandler(code = HP.code2.HOME_PUZZLE_SHOP_EXCHANGE_C_VALUE)
    public void onShopExchangeReq(HawkProtocol protocol, String playerId) {
        HomeLandPuzzleShopExchangeReq req = protocol.parseProtocol(HomeLandPuzzleShopExchangeReq.getDefaultInstance());
        HomeLandPuzzleActivity activity = getActivity(ActivityType.HOME_LAND_PUZZLE);
        Result<?> result = activity.onShopExchange(playerId, req.getCfgId(), req.getCount());
        if (result.isFail()) {
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }
}

package com.hawk.activity.type.impl.hongfugift;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

/**洪福礼包
 * @author hf
 */
public class HongFuGiftActivityHandler extends ActivityProtocolHandler {
    /**
     * 自选奖励
     * @param protocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code.HONGFU_GIFT_CHOOSE_REWARD_REQ_VALUE)
    public void chooseGiftReward(HawkProtocol protocol, String playerId){
        Activity.HongFuGiftChooseRewardIdReq req = protocol.parseProtocol(Activity.HongFuGiftChooseRewardIdReq.getDefaultInstance());
        HongFuGiftActivity activity = getActivity(ActivityType.HONG_FU_GIFT_ACTIVITY);
        Result<?> result = activity.onChooseGiftReward(playerId, req.getId(), req.getRewardId());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    /**
     * 免费解锁
     * @param protocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code.HONGFU_GIFT_FREE_BUY_REQ_VALUE)
    public void unlockFreeGift(HawkProtocol protocol, String playerId){
        Activity.HongFuGiftFreeUnlockReq req = protocol.parseProtocol(Activity.HongFuGiftFreeUnlockReq.getDefaultInstance());
        HongFuGiftActivity activity = getActivity(ActivityType.HONG_FU_GIFT_ACTIVITY);
        Result<?> result = activity.onUnlockFreeGift(playerId, req.getId());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    /**
     * 领取奖励
     * @param protocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code.HONGFU_GIFT_REC_REWARD_REQ_VALUE)
    public void receiveGiftReward(HawkProtocol protocol, String playerId){
        Activity.HongFuGiftRecRewardIdReq req = protocol.parseProtocol(Activity.HongFuGiftRecRewardIdReq.getDefaultInstance());
        HongFuGiftActivity activity = getActivity(ActivityType.HONG_FU_GIFT_ACTIVITY);
        Result<?> result = activity.onReceiveGiftReward(playerId, req.getId());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }


}

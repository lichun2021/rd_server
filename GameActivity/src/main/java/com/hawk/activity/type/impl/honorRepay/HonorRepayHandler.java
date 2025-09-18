package com.hawk.activity.type.impl.honorRepay;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.HonorRepayBuyReq;
import com.hawk.game.protocol.Activity.HonorRepayReceiveRewardReq;

/**荣耀返利活动消息处理
 * hf
 */
public class HonorRepayHandler extends ActivityProtocolHandler {

    @ProtocolHandler(code = HP.code.HONOR_REPAY_BUY_REQ_VALUE)
    public void buyHonorRepay(HawkProtocol protocol, String playerId){
        HonorRepayBuyReq req = protocol.parseProtocol(HonorRepayBuyReq.getDefaultInstance());
        HonorRepayActivity activity = getActivity(ActivityType.HONOR_REPAY_ACTIVITY);
        Result<?> result = activity.buyHonorRepay(playerId, req.getNum(), protocol.getType());
        if (result.isFail()) {
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code.HONOR_REPAY_GET_PAGE_REQ_VALUE)
    public void getHonorRepayPageInfo(HawkProtocol protocol, String playerId){
        HonorRepayActivity activity = getActivity(ActivityType.HONOR_REPAY_ACTIVITY);
        activity.syncActivityDataInfo(playerId);
    }

    @ProtocolHandler(code = HP.code.HONOR_REPAY_RECEIVE_REWARD_VALUE)
    public void receiveHonorRepayPageInfo(HawkProtocol protocol, String playerId){
        HonorRepayActivity activity = getActivity(ActivityType.HONOR_REPAY_ACTIVITY);
        HonorRepayReceiveRewardReq req = protocol.parseProtocol(HonorRepayReceiveRewardReq.getDefaultInstance());
        activity.receiveHonorRepayReward(playerId, req.getLevel(), protocol.getType());
    }
}

package com.hawk.activity.type.impl.backToNewFly;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

public class BackToNewFlyHandler extends ActivityProtocolHandler {

    @ProtocolHandler(code = HP.code2.BACK_TO_NEW_FLY_REQ_VALUE)
    public void fly(HawkProtocol protocol, String playerId){
        BackToNewFlyActivity activity = getActivity(ActivityType.BACK_TO_NEW_FLY);
        //兑换
        Result<?> result = activity.fly(playerId);
        //如果没有成功执行，返回对应错误码
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }


    @ProtocolHandler(code = HP.code2.BACK_TO_NEW_FLY_EXCHANGE_REQ_VALUE)
    public void exchange(HawkProtocol protocol, String playerId){
        BackToNewFlyActivity activity = getActivity(ActivityType.BACK_TO_NEW_FLY);
        Activity.BackToNewFlyExchangeReq req = protocol.parseProtocol(Activity.BackToNewFlyExchangeReq.getDefaultInstance());
        //兑换
        Result<?> result = activity.exchange(playerId, req.getGoodsId(), req.getExhangeTimes());
        //如果没有成功执行，返回对应错误码
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }
}

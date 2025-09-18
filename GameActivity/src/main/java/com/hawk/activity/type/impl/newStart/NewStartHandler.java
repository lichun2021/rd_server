package com.hawk.activity.type.impl.newStart;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

public class NewStartHandler extends ActivityProtocolHandler {

    @ProtocolHandler(code = HP.code2.NEW_START_INFO_REQ_VALUE)
    public void info(HawkProtocol protocol, String playerId){
        NewStartActivity activity = getActivity(ActivityType.NEW_START);
        Result<?> result = activity.info(playerId);
        //如果没有成功执行，返回对应错误码
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.NEW_START_BIND_REQ_VALUE)
    public void bind(HawkProtocol protocol, String playerId){
        NewStartActivity activity = getActivity(ActivityType.NEW_START);
        Result<?> result = activity.bind(playerId);
        //如果没有成功执行，返回对应错误码
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.NEW_START_AWARD_REQ_VALUE)
    public void award(HawkProtocol protocol, String playerId){
        Activity.NewStartAwardReq req = protocol.parseProtocol(Activity.NewStartAwardReq.getDefaultInstance());
        NewStartActivity activity = getActivity(ActivityType.NEW_START);
        Result<?> result = activity.award(playerId, req.getDay());
        //如果没有成功执行，返回对应错误码
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }
}

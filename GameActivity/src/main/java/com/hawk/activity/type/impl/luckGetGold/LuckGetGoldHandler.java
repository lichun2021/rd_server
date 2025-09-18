package com.hawk.activity.type.impl.luckGetGold;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

public class LuckGetGoldHandler extends ActivityProtocolHandler {

    @ProtocolHandler(code = HP.code2.LUCK_GET_GOLD_INFO_REQ_VALUE)
    public void info(HawkProtocol protocol, String playerId){
        LuckGetGoldActivity activity = getActivity(ActivityType.LUCK_GET_GOLD);
        Result<?> result = activity.info(playerId);
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.LUCK_GET_GOLD_CHOOSE_REQ_VALUE)
    public void choose(HawkProtocol protocol, String playerId){
        LuckGetGoldActivity activity = getActivity(ActivityType.LUCK_GET_GOLD);
        Activity.LuckGetGoldChooseReq req = protocol.parseProtocol(Activity.LuckGetGoldChooseReq.getDefaultInstance());
        Result<?> result = activity.choose(playerId, req.getPoolId());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.LUCK_GET_GOLD_DRAW_REQ_VALUE)
    public void draw(HawkProtocol protocol, String playerId){
        LuckGetGoldActivity activity = getActivity(ActivityType.LUCK_GET_GOLD);
        Activity.LuckGetGoldDrawReq req = protocol.parseProtocol(Activity.LuckGetGoldDrawReq.getDefaultInstance());
        Result<?> result = activity.draw(playerId, req.getType());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.LUCK_GET_GOLD_RECORD_REQ_VALUE)
    public void record(HawkProtocol protocol, String playerId){
        LuckGetGoldActivity activity = getActivity(ActivityType.LUCK_GET_GOLD);
        Result<?> result = activity.record(playerId);
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.LUCK_GET_GOLD_SELF_RECORD_REQ_VALUE)
    public void selfRecord(HawkProtocol protocol, String playerId){
        LuckGetGoldActivity activity = getActivity(ActivityType.LUCK_GET_GOLD);
        Result<?> result = activity.selfRecord(playerId);
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }


}

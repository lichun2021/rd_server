package com.hawk.activity.type.impl.cnyExam;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

public class CnyExamHandler extends ActivityProtocolHandler {
    @ProtocolHandler(code = HP.code2.CNY_EXAM_INFO_REQ_VALUE)
    public void info(HawkProtocol protocol, String playerId){
        CnyExamActivity activity = getActivity(ActivityType.CNY_EXAM_ACTIVITY);
        Result<?> result = activity.info(playerId);
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.CNY_EXAM_CHOOSE_REQ_VALUE)
    public void choose(HawkProtocol protocol, String playerId){
        CnyExamActivity activity = getActivity(ActivityType.CNY_EXAM_ACTIVITY);
        Activity.CNYExamChooseReq req = protocol.parseProtocol(Activity.CNYExamChooseReq.getDefaultInstance());
        Result<?> result = activity.choose(playerId, req.getLevel(), req.getChoose1(), req.getChoose2());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.CNY_EXAM_AWARD_REQ_VALUE)
    public void award(HawkProtocol protocol, String playerId){
        CnyExamActivity activity = getActivity(ActivityType.CNY_EXAM_ACTIVITY);
        Activity.CNYExamAwardReq req = protocol.parseProtocol(Activity.CNYExamAwardReq.getDefaultInstance());
        Result<?> result = activity.award(playerId, req.getLevel());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }
}

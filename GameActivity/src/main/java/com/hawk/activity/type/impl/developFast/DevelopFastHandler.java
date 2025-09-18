package com.hawk.activity.type.impl.developFast;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

/**
 * 实力飞升协议接口类
 */
public class DevelopFastHandler extends ActivityProtocolHandler {

    /**
     * 请求页面信息
     * @param protocol 前端协议
     * @param playerId 玩家id
     */
    @ProtocolHandler(code = HP.code2.DEVELOP_FAST_INFO_REQ_VALUE)
    public void info(HawkProtocol protocol, String playerId){
        DevelopFastActivity activity = getActivity(ActivityType.DEVELOP_FAST);
        Result<?> result = activity.info(playerId);
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    /**
     * 任务领奖
     * @param protocol 前端协议
     * @param playerId 玩家id
     */
    @ProtocolHandler(code = HP.code2.DEVELOP_FAST_AWARD_VALUE)
    public void award(HawkProtocol protocol, String playerId){
        Activity.DevelopFastAward award = protocol.parseProtocol(Activity.DevelopFastAward.getDefaultInstance());
        DevelopFastActivity activity = getActivity(ActivityType.DEVELOP_FAST);
        Result<?> result = activity.award(playerId, award.getTaskId());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    /**
     * 购买信息
     * @param protocol 前端协议
     * @param playerId 玩家id
     */
    @ProtocolHandler(code = HP.code2.DEVELOP_FAST_BUY_REQ_VALUE)
    public void buyInfo(HawkProtocol protocol, String playerId){
        Activity.DevelopFastBuyReq req = protocol.parseProtocol(Activity.DevelopFastBuyReq.getDefaultInstance());
        DevelopFastActivity activity = getActivity(ActivityType.DEVELOP_FAST);
        Result<?> result = activity.buyInfo(playerId,req.getType());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }
}

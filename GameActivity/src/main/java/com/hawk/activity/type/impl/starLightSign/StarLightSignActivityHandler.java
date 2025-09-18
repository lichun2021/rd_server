package com.hawk.activity.type.impl.starLightSign;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.seasonActivity.SeasonActivity;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

public class StarLightSignActivityHandler extends ActivityProtocolHandler {

    /**
     * 前端请求活动信息
     * @param protocol 前端协议
     * @param playerId 玩家id
     */
    @ProtocolHandler(code = HP.code2.STAR_LIGHT_SIGN_INFO_REQ_VALUE)
    public void infoGet(HawkProtocol protocol, String playerId){
        //获得活动
        StarLightSignActivity activity = getActivity(ActivityType.WORLD_HONOR_ACTIVITY);
        //同步数据
        activity.syncActivityDataInfo(playerId);
    }


    @ProtocolHandler(code = HP.code2.STAR_LIGHT_SIGN_REQ_VALUE)
    public void sign(HawkProtocol protocol, String playerId){
        //获得活动
        StarLightSignActivity activity = getActivity(ActivityType.WORLD_HONOR_ACTIVITY);
        Activity.StarlightSignReq req = protocol.parseProtocol(Activity.StarlightSignReq.getDefaultInstance());
        //同步数据
        Result<?> result = activity.sign(playerId, req.getDay());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.STAR_LIGHT_SIGN_REDEEM_REQ_VALUE)
    public void signRedeem(HawkProtocol protocol, String playerId){
        //获得活动
        StarLightSignActivity activity = getActivity(ActivityType.WORLD_HONOR_ACTIVITY);
        Activity.StarlightSignRedeemReq req = protocol.parseProtocol(Activity.StarlightSignRedeemReq.getDefaultInstance());
        //同步数据
        Result<?> result = activity.signRedeem(playerId, req.getDay());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.STAR_LIGHT_SIGN_CHOOSE_REQ_VALUE)
    public void choose(HawkProtocol protocol, String playerId){
        //获得活动
        StarLightSignActivity activity = getActivity(ActivityType.WORLD_HONOR_ACTIVITY);
        Activity.StarlightSignChooseReq req = protocol.parseProtocol(Activity.StarlightSignChooseReq.getDefaultInstance());
        //同步数据
        Result<?> result = activity.choose(playerId, req.getType(), req.getRechargeType(), req.getItemId());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.STAR_LIGHT_SIGN_AWARD_REQ_VALUE)
    public void award(HawkProtocol protocol, String playerId){
        //获得活动
        StarLightSignActivity activity = getActivity(ActivityType.WORLD_HONOR_ACTIVITY);
        Activity.StarlightSignAwardReq req = protocol.parseProtocol(Activity.StarlightSignAwardReq.getDefaultInstance());
        //同步数据
        Result<?> result = activity.award(playerId, req.getType());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.STAR_LIGHT_SIGN_BUY_REQ_VALUE)
    public void buyScore(HawkProtocol protocol, String playerId){
        //获得活动
        StarLightSignActivity activity = getActivity(ActivityType.WORLD_HONOR_ACTIVITY);
        Activity.StarlightSignBuyReq req = protocol.parseProtocol(Activity.StarlightSignBuyReq.getDefaultInstance());
        //同步数据
        Result<?> result = activity.buyScore(playerId, req.getScore());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }else{
            responseSuccess(playerId, protocol.getType());
        }

    }

    @ProtocolHandler(code = HP.code2.STAR_LIGHT_SIGN_MULTIPLE_REQ_VALUE)
    public void multiple(HawkProtocol protocol, String playerId){
        //获得活动
        StarLightSignActivity activity = getActivity(ActivityType.WORLD_HONOR_ACTIVITY);
        Activity.StarlightSignMultipleReq req = protocol.parseProtocol(Activity.StarlightSignMultipleReq.getDefaultInstance());
        //同步数据
        Result<?> result = activity.multiple(playerId, req.getType(), req.getRechargeType());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.STAR_LIGHT_SIGN_SCORE_BOX_REQ_VALUE)
    public void scoreBox(HawkProtocol protocol, String playerId){
        //获得活动
        StarLightSignActivity activity = getActivity(ActivityType.WORLD_HONOR_ACTIVITY);
        Activity.StarlightSignScoreBoxReq req = protocol.parseProtocol(Activity.StarlightSignScoreBoxReq.getDefaultInstance());
        //同步数据
        Result<?> result = activity.scoreBox(playerId, req.getId());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }
}

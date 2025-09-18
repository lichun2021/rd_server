package com.hawk.activity.type.impl.pddActivity;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

public class PDDActivityHandler extends ActivityProtocolHandler {
    @ProtocolHandler(code = HP.code2.PDD_INFO_REQ_VALUE)
    public void info(HawkProtocol protocol, String playerId){
        PDDActivity activity = getActivity(ActivityType.PDD_ACTIVITY);
        Result<?> result = activity.info(playerId);
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
        activity.sendFirstTip(playerId);
    }

    @ProtocolHandler(code = HP.code2.PDD_ORDER_LIST_REQ_VALUE)
    public void orderList(HawkProtocol protocol, String playerId){
        PDDActivity activity = getActivity(ActivityType.PDD_ACTIVITY);
        Result<?> result = activity.orderList(playerId);
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.PDD_ORDER_INFO_REQ_VALUE)
    public void orderInfo(HawkProtocol protocol, String playerId){
        PDDActivity activity = getActivity(ActivityType.PDD_ACTIVITY);
        Activity.PDDOrderInfoReq req = protocol.parseProtocol(Activity.PDDOrderInfoReq.getDefaultInstance());
        Result<?> result = activity.orderInfo(playerId, req.getPlayerId(), req.getOrderId());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.PDD_GROUP_LIST_REQ_VALUE)
    public void groupList(HawkProtocol protocol, String playerId){
        PDDActivity activity = getActivity(ActivityType.PDD_ACTIVITY);
        Activity.PDDGroupListReq req = protocol.parseProtocol(Activity.PDDGroupListReq.getDefaultInstance());
        Result<?> result = activity.groupList(playerId, req.getCfgId());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.PDD_BUY_REQ_VALUE)
    public void buy(HawkProtocol protocol, String playerId){
        PDDActivity activity = getActivity(ActivityType.PDD_ACTIVITY);
        Activity.PDDBuyReq req = protocol.parseProtocol(Activity.PDDBuyReq.getDefaultInstance());
        Activity.PDDBuyType type = req.getType();
        int cfgId = req.getCfgId();
        String targetPlayerId = req.hasPlayerId() ? req.getPlayerId() : "";
        String orderId = req.hasOrderId() ? req.getOrderId() : "";
        boolean isShare = req.hasIsShare() ? req.getIsShare() : false;
        Activity.PDDShareType shareType = req.hasShareType() ? req.getShareType() : Activity.PDDShareType.PDD_GUILD;
        Result<?> result = activity.buy(playerId , type, cfgId, targetPlayerId, orderId, isShare, shareType);
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }else {
            responseSuccess(playerId, protocol.getType());
        }
    }

    @ProtocolHandler(code = HP.code2.PDD_SHARE_REQ_VALUE)
    public void share(HawkProtocol protocol, String playerId){
        PDDActivity activity = getActivity(ActivityType.PDD_ACTIVITY);
        Activity.PDDShareReq req = protocol.parseProtocol(Activity.PDDShareReq.getDefaultInstance());
        Activity.PDDShareType type = req.hasType() ? req.getType() : Activity.PDDShareType.PDD_GUILD;
        Result<?> result = activity.share(playerId, req.getOrderId(), type);
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.PDD_CANCEL_REQ_VALUE)
    public void cancel(HawkProtocol protocol, String playerId){
        PDDActivity activity = getActivity(ActivityType.PDD_ACTIVITY);
        Activity.PDDCancelReq req = protocol.parseProtocol(Activity.PDDCancelReq.getDefaultInstance());
        Result<?> result = activity.cancel(playerId, req.getOrderId());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }else {
            responseSuccess(playerId, protocol.getType());
        }
    }

    @ProtocolHandler(code = HP.code2.PDD_AWARD_REQ_VALUE)
    public void award(HawkProtocol protocol, String playerId){
        PDDActivity activity = getActivity(ActivityType.PDD_ACTIVITY);
        Activity.PDDAwardReq req = protocol.parseProtocol(Activity.PDDAwardReq.getDefaultInstance());
        Result<?> result = activity.award(playerId, req.getOrderId());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }else {
            responseSuccess(playerId, protocol.getType());
        }
    }
}

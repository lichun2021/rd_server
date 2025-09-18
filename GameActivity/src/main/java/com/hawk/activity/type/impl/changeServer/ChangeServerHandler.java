package com.hawk.activity.type.impl.changeServer;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

public class ChangeServerHandler extends ActivityProtocolHandler {
    @ProtocolHandler(code = HP.code2.CHANGE_SVR_ACTIVITY_INFO_REQ_VALUE)
    public void info(HawkProtocol protocol, String playerId){
        ChangeServerActivity activity = getActivity(ActivityType.CHANGE_SVR_ACTIVITY);
        Result<?> result = activity.info(playerId);
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }


    @ProtocolHandler(code = HP.code2.CHANGE_SVR_ACTIVITY_SCORE_RANK_REQ_VALUE)
    public void scoreRank(HawkProtocol protocol, String playerId){
        ChangeServerActivity activity = getActivity(ActivityType.CHANGE_SVR_ACTIVITY);
        Result<?> result = activity.scoreRank(playerId);
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.CHANGE_SVR_ACTIVITY_MANAGER_LIST_REQ_VALUE)
    public void managerList(HawkProtocol protocol, String playerId){
        ChangeServerActivity activity = getActivity(ActivityType.CHANGE_SVR_ACTIVITY);
        Result<?> result = activity.managerList(playerId);
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.CHANGE_SVR_ACTIVITY_INTIVEE_LIST_REQ_VALUE)
    public void intiveeList(HawkProtocol protocol, String playerId){
        ChangeServerActivity activity = getActivity(ActivityType.CHANGE_SVR_ACTIVITY);
        Result<?> result = activity.intiveeList(playerId);
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.CHANGE_SVR_ACTIVITY_APPLY_LIST_REQ_VALUE)
    public void applyList(HawkProtocol protocol, String playerId){
        ChangeServerActivity activity = getActivity(ActivityType.CHANGE_SVR_ACTIVITY);
        Result<?> result = activity.applyList(playerId);
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.CHANGE_SVR_ACTIVITY_POWER_REQ_VALUE)
    public void power(HawkProtocol protocol, String playerId){
        ChangeServerActivity activity = getActivity(ActivityType.CHANGE_SVR_ACTIVITY);
        Result<?> result = activity.power(playerId);
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }


    @ProtocolHandler(code = HP.code2.CHANGE_SVR_ACTIVITY_INVITE_REQ_VALUE)
    public void invite(HawkProtocol protocol, String playerId){
        ChangeServerActivity activity = getActivity(ActivityType.CHANGE_SVR_ACTIVITY);
        Activity.ChangeServerActivityInviteReq req = protocol.parseProtocol(Activity.ChangeServerActivityInviteReq.getDefaultInstance());
        Result<?> result = activity.invite(playerId, req.getPlayerId());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.CHANGE_SVR_ACTIVITY_APPROVE_REQ_VALUE)
    public void approve(HawkProtocol protocol, String playerId){
        ChangeServerActivity activity = getActivity(ActivityType.CHANGE_SVR_ACTIVITY);
        Activity.ChangeServerActivityApproveReq req = protocol.parseProtocol(Activity.ChangeServerActivityApproveReq.getDefaultInstance());
        Result<?> result = activity.approve(playerId, req.getPlayerId(), req.getIsPass());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.CHANGE_SVR_ACTIVITY_CANCEL_REQ_VALUE)
    public void cancel(HawkProtocol protocol, String playerId){
        ChangeServerActivity activity = getActivity(ActivityType.CHANGE_SVR_ACTIVITY);
        Activity.ChangeServerActivityCancelReq req = protocol.parseProtocol(Activity.ChangeServerActivityCancelReq.getDefaultInstance());
        Result<?> result = activity.cancel(playerId, req.getPlayerId());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.CHANGE_SVR_ACTIVITY_SHOW_REQ_VALUE)
    public void show(HawkProtocol protocol, String playerId){
        ChangeServerActivity activity = getActivity(ActivityType.CHANGE_SVR_ACTIVITY);
        Result<?> result = activity.show(playerId);
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }


    @ProtocolHandler(code = HP.code2.CHANGE_SVR_ACTIVITY_CHANGE_CHECK_REQ_VALUE)
    public void changeCheck(HawkProtocol protocol, String playerId){
        ChangeServerActivity activity = getActivity(ActivityType.CHANGE_SVR_ACTIVITY);
        Result<?> result = activity.changeCheck(playerId);
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.CHANGE_SVR_ACTIVITY_CHANGE_REQ_VALUE)
    public void change(HawkProtocol protocol, String playerId){
        ChangeServerActivity activity = getActivity(ActivityType.CHANGE_SVR_ACTIVITY);
        Result<?> result = activity.change(playerId);
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.CHANGE_SVR_ACTIVITY_SEARCH_REQ_VALUE)
    public void search(HawkProtocol protocol, String playerId){
        ChangeServerActivity activity = getActivity(ActivityType.CHANGE_SVR_ACTIVITY);
        Activity.ChangeServerActivitySearchReq req = protocol.parseProtocol(Activity.ChangeServerActivitySearchReq.getDefaultInstance());
        Result<?> result = activity.search(playerId,protocol.getType(), req.hasName()?req.getName():"",req.getType());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }
}

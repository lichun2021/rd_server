package com.hawk.activity.type.impl.shareprosperity;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.ShareProperityBindRoleReq;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

/**
 * 有福同享 376活动 （新服充值给老服返利）
 * @author lating
 *
 */
public class ShareProsperityHandler extends ActivityProtocolHandler {

    @ProtocolHandler(code = HP.code2.SHARE_PROS_ACTIVITY_INFO_C_VALUE)
    public void info(HawkProtocol protocol, String playerId){
        ShareProsperityActivity activity = getActivity(ActivityType.SHARE_PROSPERITY_376);
        activity.syncActivityDataInfo(playerId);
    }

    @ProtocolHandler(code = HP.code2.SHARE_PROS_BIND_ROLE_C_VALUE)
    public void bind(HawkProtocol protocol, String playerId){
        ShareProsperityActivity activity = getActivity(ActivityType.SHARE_PROSPERITY_376);
        ShareProperityBindRoleReq req = protocol.parseProtocol(ShareProperityBindRoleReq.getDefaultInstance());
        Result<?> result = activity.bind(playerId, req.getPlayerId());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        } else {
        	responseSuccess(playerId, protocol.getType());
        }
    }

}

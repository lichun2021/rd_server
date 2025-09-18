package com.hawk.activity.type.impl.diffNewServerTech;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

public class DiffNewServerTechHandler extends ActivityProtocolHandler {

    /**
     * 领奖
     * @param protocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code2.DIFF_NEW_SERVER_TECH_AWARD_VALUE)
    public void award(HawkProtocol protocol, String playerId){
        DiffNewServerTechActivity activity = getActivity(ActivityType.DIFF_NEW_SERVER_TECH);
        Activity.DiffNewServerTechAward req = protocol.parseProtocol(Activity.DiffNewServerTechAward.getDefaultInstance());
        Result<?> result = activity.award(playerId, req.getCfgId());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }
}

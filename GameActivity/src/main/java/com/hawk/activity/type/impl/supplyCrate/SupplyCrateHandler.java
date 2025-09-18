package com.hawk.activity.type.impl.supplyCrate;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

public class SupplyCrateHandler extends ActivityProtocolHandler {
    @ProtocolHandler(code = HP.code2.SUPPLY_CRATE_INFO_REQ_VALUE)
    public void info(HawkProtocol protocol, String playerId){
        SupplyCrateActivity activity = getActivity(ActivityType.SUPPLY_CRATE);
        Result<?> result = activity.info(playerId);
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.SUPPLY_CRATE_CHOOSE_REQ_VALUE)
    public void choose(HawkProtocol protocol, String playerId){
        Activity.SupplyCrateChooseReq req = protocol.parseProtocol(Activity.SupplyCrateChooseReq.getDefaultInstance());
        SupplyCrateActivity activity = getActivity(ActivityType.SUPPLY_CRATE);
        Result<?> result = activity.choose(playerId, req.getIndex());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.SUPPLY_CRATE_READY_REQ_VALUE)
    public void ready(HawkProtocol protocol, String playerId){
        SupplyCrateActivity activity = getActivity(ActivityType.SUPPLY_CRATE);
        Result<?> result = activity.ready(playerId);
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.SUPPLY_CRATE_OPEN_REQ_VALUE)
    public void open(HawkProtocol protocol, String playerId){
        Activity.SupplyCrateOpenReq req = protocol.parseProtocol(Activity.SupplyCrateOpenReq.getDefaultInstance());
        SupplyCrateActivity activity = getActivity(ActivityType.SUPPLY_CRATE);
        Result<?> result = activity.open(playerId, req.getPos());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.SUPPLY_CRATE_NEXT_REQ_VALUE)
    public void next(HawkProtocol protocol, String playerId){
        SupplyCrateActivity activity = getActivity(ActivityType.SUPPLY_CRATE);
        Result<?> result = activity.next(playerId);
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.SUPPLY_CRATE_BUY_REQ_VALUE)
    public void buy(HawkProtocol protocol, String playerId){
        Activity.SupplyCrateBuyReq req = protocol.parseProtocol(Activity.SupplyCrateBuyReq.getDefaultInstance());
        SupplyCrateActivity activity = getActivity(ActivityType.SUPPLY_CRATE);
        Result<?> result = activity.buy(playerId, req.getCount());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.SUPPLY_CRATE_GUILD_BOX_REQ_VALUE)
    public void guildBox(HawkProtocol protocol, String playerId){
        SupplyCrateActivity activity = getActivity(ActivityType.SUPPLY_CRATE);
        Result<?> result = activity.guildBox(playerId);
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.SUPPLY_CRATE_RANK_REQ_VALUE)
    public void rank(HawkProtocol protocol, String playerId){
        SupplyCrateActivity activity = getActivity(ActivityType.SUPPLY_CRATE);
        Result<?> result = activity.rank(playerId);
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }
}

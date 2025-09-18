package com.hawk.activity.type.impl.returnUpgrade;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

public class ReturnUpgradeHandler extends ActivityProtocolHandler {

    @ProtocolHandler(code = HP.code2.RETURN_UPGRADE_REQ_VALUE)
    public void upgrade(HawkProtocol protocol, String playerId){
        Activity.ReturnUpgradeReq req = protocol.parseProtocol(Activity.ReturnUpgradeReq.getDefaultInstance());
        ReturnUpgradeActivity activity = getActivity(ActivityType.RETURN_UPGRADE);
        //兑换
        Result<?> result = activity.upgrade(playerId, req.getType(), req.getUseGold());
        //如果没有成功执行，返回对应错误码
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.RETURN_UPGRADE_EXCHANGE_REQ_VALUE)
    public void exchange(HawkProtocol protocol, String playerId){
        Activity.ReturnUpgradeExchangeReq req = protocol.parseProtocol(Activity.ReturnUpgradeExchangeReq.getDefaultInstance());
        ReturnUpgradeActivity activity = getActivity(ActivityType.RETURN_UPGRADE);
        //兑换
        Result<?> result = activity.exchange(playerId, req.getGoodsId(), req.getExhangeTimes());
        //如果没有成功执行，返回对应错误码
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }


    @ProtocolHandler(code = HP.code2.RETURN_UPGRADE_BUY_REQ_VALUE)
    public void buy(HawkProtocol protocol, String playerId){
        Activity.ReturnUpgradeBuyReq req = protocol.parseProtocol(Activity.ReturnUpgradeBuyReq.getDefaultInstance());
        ReturnUpgradeActivity activity = getActivity(ActivityType.RETURN_UPGRADE);
        //兑换
        Result<?> result = activity.buy(playerId, req.getBuyTimes());
        //如果没有成功执行，返回对应错误码
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.RETURN_UPGRADE_TECH_REQ_VALUE)
    public void techInfo(HawkProtocol protocol, String playerId){
        ReturnUpgradeActivity activity = getActivity(ActivityType.RETURN_UPGRADE);
        //科技信息
        Result<?> result = activity.techInfo(playerId);
        //如果没有成功执行，返回对应错误码
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }
}

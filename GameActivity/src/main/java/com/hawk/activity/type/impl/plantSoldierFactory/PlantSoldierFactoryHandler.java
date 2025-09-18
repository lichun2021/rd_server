package com.hawk.activity.type.impl.plantSoldierFactory;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

public class PlantSoldierFactoryHandler  extends ActivityProtocolHandler {

    @ProtocolHandler(code = HP.code2.PLANT_SOLDIER_FACTORY_INFO_REQ_VALUE)
    public void info(HawkProtocol protocol, String playerId) {
        PlantSoldierFactoryActivity activity = getActivity(ActivityType.PLANT_SOLDIER_FACTORY);
        Result<?> result = activity.info(playerId);
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.PLANT_SOLDIER_FACTORY_DRAW_REQ_VALUE)
    public void draw(HawkProtocol protocol, String playerId) {
        PlantSoldierFactoryActivity activity = getActivity(ActivityType.PLANT_SOLDIER_FACTORY);
        Result<?> result = activity.draw(playerId);
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.PLANT_SOLDIER_FACTORY_AWARD_REQ_VALUE)
    public void award(HawkProtocol protocol, String playerId) {
        Activity.PlantSoldierFactoryAwardReq req = protocol.parseProtocol(Activity.PlantSoldierFactoryAwardReq.getDefaultInstance());
        PlantSoldierFactoryActivity activity = getActivity(ActivityType.PLANT_SOLDIER_FACTORY);
        Result<?> result = activity.award(playerId, req.getBigPos());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.PLANT_SOLDIER_FACTORY_REFRESH_REQ_VALUE)
    public void refresh(HawkProtocol protocol, String playerId) {
        PlantSoldierFactoryActivity activity = getActivity(ActivityType.PLANT_SOLDIER_FACTORY);
        Result<?> result = activity.refresh(playerId);
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }

    @ProtocolHandler(code = HP.code2.PLANT_SOLDIER_FACTORY_SHOP_BUY_REQ_VALUE)
    public void shopBuy(HawkProtocol protocol, String playerId) {
        Activity.PlantSoldierFactoryShopBuyReq req = protocol.parseProtocol(Activity.PlantSoldierFactoryShopBuyReq.getDefaultInstance());
        PlantSoldierFactoryActivity activity = getActivity(ActivityType.PLANT_SOLDIER_FACTORY);
        Result<?> result = activity.shopBuy(playerId, req.getShopId(), req.getCount());
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }
}

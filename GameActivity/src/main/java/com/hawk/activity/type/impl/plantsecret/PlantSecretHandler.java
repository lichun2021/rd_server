package com.hawk.activity.type.impl.plantsecret;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.PlantSecretBuyItemReq;
import com.hawk.game.protocol.Activity.PlantSecretChatShareReq;
import com.hawk.game.protocol.Activity.PlantSecretOpenBoxReq;
import com.hawk.game.protocol.Activity.PlantSecretOpenCardReq;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

/**
 * 泰能机密活动
 *
 * @author lating
 */
public class PlantSecretHandler extends ActivityProtocolHandler {


    @ProtocolHandler(code = HP.code2.PLANT_SECRET_OPEN_CARD_REQ_VALUE)
    public void openCardReq(HawkProtocol protocol, String playerId) {
        PlantSecretOpenCardReq req = protocol.parseProtocol(PlantSecretOpenCardReq.getDefaultInstance());
        PlantSecretActivity activity = this.getActivity(ActivityType.PLANT_SECRET_ACTIVITY);
        Result<?> result = activity.openCard(playerId, req.getCardId());
        if (result.isFail()) {
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        } else {
            responseSuccess(playerId, protocol.getType());
        }
    }


    @ProtocolHandler(code = HP.code2.PLANT_SECRET_OPEN_BOX_REQ_VALUE)
    public void openBoxReq(HawkProtocol protocol, String playerId) {
        PlantSecretOpenBoxReq req = protocol.parseProtocol(PlantSecretOpenBoxReq.getDefaultInstance());
        PlantSecretActivity activity = this.getActivity(ActivityType.PLANT_SECRET_ACTIVITY);
        Result<?> result = activity.openBox(playerId, req.getSecretNum());
        if (result.isFail()) {
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        } else {
            responseSuccess(playerId, protocol.getType());
        }
    }

    @ProtocolHandler(code = HP.code2.PLANT_SECRET_BUY_ITEM_REQ_VALUE)
    public void buyItemReq(HawkProtocol protocol, String playerId) {
        PlantSecretBuyItemReq req = protocol.parseProtocol(PlantSecretBuyItemReq.getDefaultInstance());
        PlantSecretActivity activity = this.getActivity(ActivityType.PLANT_SECRET_ACTIVITY);
        Result<?> result = activity.buyItem(playerId, req.getCount());
        if (result.isFail()) {
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        } else {
            responseSuccess(playerId, protocol.getType());
        }
    }

    @ProtocolHandler(code = HP.code2.PLANT_SECRET_CHAT_SHARE_REQ_VALUE)
    public void chatShareReq(HawkProtocol protocol, String playerId) {
        PlantSecretChatShareReq req = protocol.parseProtocol(PlantSecretChatShareReq.getDefaultInstance());
        PlantSecretActivity activity = this.getActivity(ActivityType.PLANT_SECRET_ACTIVITY);

        Result<?> result = activity.chatShare(req.getChatType(), req.getChatMsg(), playerId);
        if (result.isFail()) {
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        } else {
            responseSuccess(playerId, protocol.getType());
        }
    }
}
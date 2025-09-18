package com.hawk.activity.type.impl.midAutumn;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.MidAutumnExchangeReq;
import com.hawk.game.protocol.Activity.MidAutumnExchangeTipsReq;
import com.hawk.game.protocol.Activity.MidAutumnGiftBuyReq;
import com.hawk.game.protocol.HP;

/**中秋庆典消息
 * @author Winder
 */
public class MidAutumnHandler extends ActivityProtocolHandler{
	//兑换物品
	@ProtocolHandler(code=HP.code.MID_AUTUMN_EXCHANGE_REQ_VALUE)
	public void onExchange(HawkProtocol protocol, String playerId){
		MidAutumnExchangeReq pExchangeReq = protocol.parseProtocol(MidAutumnExchangeReq.getDefaultInstance());
		MidAutumnActivity activity = this.getActivity(ActivityType.MID_AUTUMN_ACTIVITY);
		if (activity.isAllowOprate(playerId)) {
			Result<Void> result = activity.exchange(playerId, pExchangeReq.getExchangeId(), pExchangeReq.getNum());
			if (result.isOk()) {
				this.responseSuccess(playerId, protocol.getType());
			}else {
				this.sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			}
		}
	}
	//购买礼包
	@ProtocolHandler(code = HP.code.MID_AUTUMN_BUY_GIFT_REQ_VALUE)
	public void onBuyGift(HawkProtocol protocol, String playerId){
		MidAutumnGiftBuyReq buyReq = protocol.parseProtocol(MidAutumnGiftBuyReq.getDefaultInstance());
		MidAutumnActivity activity = this.getActivity(ActivityType.MID_AUTUMN_ACTIVITY);
		if (activity.isAllowOprate(playerId)) {
			Result<Void> result = activity.buyGift(playerId, buyReq);
			if (result.isOk()) {
				this.responseSuccess(playerId, protocol.getType());
			}else {
				this.sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			}
		}	
	}

	/***
	 * 前端兑换提醒勾勾
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.MID_AUTUMN_EXCHANGE_TIPS_REQ_VALUE)
	public void midAutumnExchangePlayerTips(HawkProtocol protocol, String playerId){
		MidAutumnActivity activity = getActivity(ActivityType.MID_AUTUMN_ACTIVITY);
		MidAutumnExchangeTipsReq req = protocol.parseProtocol(MidAutumnExchangeTipsReq.getDefaultInstance());
		int id = req.getId();
		int tip = req.getTip();
		Result<?> result = activity.reqActivityTips(playerId, id, tip);
		if(result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
	
	
}

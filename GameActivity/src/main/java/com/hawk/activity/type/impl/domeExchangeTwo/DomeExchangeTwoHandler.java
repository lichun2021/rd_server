package com.hawk.activity.type.impl.domeExchangeTwo;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.DomeExchangeMsg;
import com.hawk.game.protocol.Activity.domeExchangeTips;
import com.hawk.game.protocol.HP;

public class DomeExchangeTwoHandler extends ActivityProtocolHandler {
	
	/***
	 * 穹顶兑换 前端兑换提醒勾勾
	 * @param protocol
	 * @param playerId
	 */
	//@ProtocolHandler(code = HP.code.DOME_EXCHANGE_TWO_TIPS_C_VALUE)
	public void domeExchangePlayerTips(HawkProtocol protocol, String playerId){
		DomeExchangeTwoActivity activity = getActivity(ActivityType.DOME_EXCHANGE_TWO);
		domeExchangeTips req = protocol.parseProtocol(domeExchangeTips.getDefaultInstance());
		int id = req.getId();
		int tip = req.getTip();
		Result<?> result = activity.reqActivityTips(playerId, id, tip);
		if(result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
	
	/***
	 * 穹顶兑换 兑换接口
	 * @param protocol
	 * @param playerId
	 */
	//@ProtocolHandler(code = HP.code.DOME_EXCHANGE_TWO_EXCHANGE_VALUE)
	public void domeExchangeExchange(HawkProtocol protocol, String playerId){
		DomeExchangeTwoActivity activity = getActivity(ActivityType.DOME_EXCHANGE_TWO);
		DomeExchangeMsg msg = protocol.parseProtocol(DomeExchangeMsg.getDefaultInstance());
		int count = msg.getNum();
		if (count <= 0) {
			throw new RuntimeException("zeor input");
		}
		Result<Integer> result = activity.brokenExchange(playerId, msg.getExchangeId(),
				count);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
}

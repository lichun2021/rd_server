package com.hawk.activity.type.impl.commonExchangeTwo;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.DomeExchangeMsg;
import com.hawk.game.protocol.Activity.SupplyStationBuyReq;
import com.hawk.game.protocol.Activity.domeExchangeTips;
import com.hawk.game.protocol.HP;

public class CommonExchangeTwoHandler extends ActivityProtocolHandler {
	
	/***
	 * 穹顶兑换 前端兑换提醒勾勾
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.COMMON_EXCHANGE_TWO_TIPS_C_VALUE)
	public void domeExchangePlayerTips(HawkProtocol protocol, String playerId){
		CommonExchangeTwoActivity activity = getActivity(ActivityType.COMMON_EXCHANGE_TWO_ACTIVITY);
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
	@ProtocolHandler(code = HP.code.COMMON_EXCHANGE_TWO_EXCHANGE_VALUE)
	public void domeExchangeExchange(HawkProtocol protocol, String playerId){
		CommonExchangeTwoActivity activity = getActivity(ActivityType.COMMON_EXCHANGE_TWO_ACTIVITY);
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
	
	@ProtocolHandler(code = HP.code.COMMON_EXCHANGE_TWO_BUY_C_VALUE)
	public void onPlayerBuySupplyBox(HawkProtocol protocol, String playerId){
		CommonExchangeTwoActivity activity = getActivity(ActivityType.COMMON_EXCHANGE_TWO_ACTIVITY);
		SupplyStationBuyReq req = protocol.parseProtocol(SupplyStationBuyReq.getDefaultInstance());
		int chestId = req.getId();
		int count = req.getCount();
		if(count <= 0 || chestId <= 0){
			HawkLog.errPrintln("SupplyStationBuyReq error, chestId:{}, countId:{}", chestId, count);
			return;
		}
		Result<?> result = activity.onPlayerBuyChest(chestId, count, playerId);
		if(result != null && result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
}

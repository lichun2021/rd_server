package com.hawk.activity.type.impl.supplyStationTwo;

import com.hawk.game.protocol.Activity;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.SupplyStationBuyReq;
import com.hawk.game.protocol.HP;

public class SupplyStationTwoHandler extends ActivityProtocolHandler {
	
	@ProtocolHandler(code = HP.code.SUPPLY_STATION_TWO_BUY_C_VALUE)
	public void onPlayerBuySupplyBox(HawkProtocol protocol, String playerId){
		SupplyStationTwoActivity activity = getActivity(ActivityType.SUPPLY_STATION_TWO_ACTIVITY);
		SupplyStationBuyReq req = protocol.parseProtocol(SupplyStationBuyReq.getDefaultInstance());
		int chestId = req.getId();
		int count = req.getCount();
		if(count <= 0 || chestId <= 0){
			HawkLog.errPrintln("SupplyStationTwoBuyReq error, chestId:{}, countId:{}", chestId, count);
			return;
		}
		Result<?> result = activity.onPlayerBuyChest(chestId, count, playerId);
		if(result != null && result.isFail()){
			sendErrorAndBreak(playerId, HP.code.SUPPLY_STATION_TWO_BUY_C_VALUE, result.getStatus());
		}
	}

	/***
	 * 穹顶兑换 前端兑换提醒勾勾
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.DOME_EXCHANGE_TWO_TIPS_C_VALUE)
	public void domeExchangePlayerTips(HawkProtocol protocol, String playerId){
		SupplyStationTwoActivity activity = getActivity(ActivityType.SUPPLY_STATION_TWO_ACTIVITY);
		Activity.domeExchangeTips req = protocol.parseProtocol(Activity.domeExchangeTips.getDefaultInstance());
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
	@ProtocolHandler(code = HP.code.DOME_EXCHANGE_TWO_EXCHANGE_VALUE)
	public void domeExchangeExchange(HawkProtocol protocol, String playerId){
		SupplyStationTwoActivity activity = getActivity(ActivityType.SUPPLY_STATION_TWO_ACTIVITY);
		Activity.DomeExchangeMsg msg = protocol.parseProtocol(Activity.DomeExchangeMsg.getDefaultInstance());
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

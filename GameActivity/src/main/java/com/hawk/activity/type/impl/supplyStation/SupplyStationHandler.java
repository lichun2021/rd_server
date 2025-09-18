package com.hawk.activity.type.impl.supplyStation;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.SupplyStationBuyReq;
import com.hawk.game.protocol.HP;

public class SupplyStationHandler extends ActivityProtocolHandler {
	
	@ProtocolHandler(code = HP.code.SUPPLY_STATION_BUY_C_VALUE)
	public void onPlayerBuySupplyBox(HawkProtocol protocol, String playerId){
		SupplyStationActivity activity = getActivity(ActivityType.SUPPLY_STATION_ACTIVITY);
		SupplyStationBuyReq req = protocol.parseProtocol(SupplyStationBuyReq.getDefaultInstance());
		int chestId = req.getId();
		int count = req.getCount();
		if(count <= 0 || chestId <= 0){
			HawkLog.errPrintln("SupplyStationBuyReq error, chestId:{}, countId:{}", chestId, count);
			return;
		}
		Result<?> result = activity.onPlayerBuyChest(chestId, count, playerId);
		if(result != null && result.isFail()){
			sendErrorAndBreak(playerId, HP.code.SUPPLY_STATION_BUY_C_VALUE, result.getStatus());
		}
	}
}

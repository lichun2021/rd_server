package com.hawk.activity.type.impl.supplyStationCopy;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.game.protocol.HP;

public class SupplyStationCopyHandler extends ActivityProtocolHandler {

	@ProtocolHandler(code = HP.code.SUPPLY_STATION_COPY_BUY_C_VALUE)
	public void onPlayerBuySupplyBox(HawkProtocol protocol, String playerId){
		
	}
}

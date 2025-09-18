package com.hawk.activity.type.impl.armamentexchange;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.ArmamentExchangeReq;
import com.hawk.game.protocol.HP;

/**
 * 周年商城
 * @author luke
 */
public class ArmamentExchangeHandler extends ActivityProtocolHandler {
	@ProtocolHandler(code = HP.code.ARMAMENT_MAIN_INFO_REQ_VALUE)
	public void main(HawkProtocol hawkProtocol, String playerId){
		ArmamentExchangeActivity activity = this.getActivity(ActivityType.ARMAMENT_EXCHANGE_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.syncActivityDataInfo(playerId);
	}
	
	@ProtocolHandler(code = HP.code.ARMAMENT_EXCHANGE_REQ_VALUE)
	public void exchange(HawkProtocol hawkProtocol, String playerId){
		ArmamentExchangeActivity activity = this.getActivity(ActivityType.ARMAMENT_EXCHANGE_ACTIVITY);
		if(activity == null){
			return;
		}
		ArmamentExchangeReq req = hawkProtocol.parseProtocol(ArmamentExchangeReq.getDefaultInstance());
		activity.exchange(playerId,req);
	}
}
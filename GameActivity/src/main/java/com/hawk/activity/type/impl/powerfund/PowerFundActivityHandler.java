package com.hawk.activity.type.impl.powerfund;


import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;

/**
 * 直购基金活动
 * 
 * @author lating
 *
 */
public class PowerFundActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 购买直购基金
	 * 
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.POWER_FUND_BUY_VALUE)
	public boolean onPowerFundBuy(HawkProtocol protocol, String playerId) {
		PowerFundActivity activity = getActivity(ActivityType.POWER_FUND_ACTIVITY);
		Result<?> result = activity.buyPowerFund(playerId);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return false;
		}
		responseSuccess(playerId, protocol.getType());
		return true;
	}

}

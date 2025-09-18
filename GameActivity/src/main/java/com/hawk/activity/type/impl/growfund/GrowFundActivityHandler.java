package com.hawk.activity.type.impl.growfund;


import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;

/**
 * 等级基金活动网络消息接收句柄
 * 
 * @author PhilChen
 *
 */
public class GrowFundActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 购买等级基金
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GROW_FUND_BUY_VALUE)
	public boolean onGrowfundBuy(HawkProtocol protocol, String playerId) {
		GrowFundActivity activity = getActivity(ActivityType.GROW_FUND_ACTIVITY);
		Result<?> result = activity.buyGrowfund(playerId);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return false;
		}
		responseSuccess(playerId, protocol.getType());
		return true;
	}

}

package com.hawk.activity.type.impl.fullyArmed;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.HPFullyArmedActivityBuy;

public class FullyArmedHandler extends ActivityProtocolHandler {

	/**
	 * 购买探测仪
	 * 
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.HP_FULLY_ARMED_BUY_REQ_C_VALUE)
	public boolean onBuySearchItem(HawkProtocol protocol, String playerId) {
		HPFullyArmedActivityBuy msg = protocol.parseProtocol(HPFullyArmedActivityBuy.getDefaultInstance());
		FullyArmedActivity activity = getActivity(ActivityType.FULLY_ARMED_ACTIVITY);
		if (null != activity && null != msg) {
			Result<?> result = activity.onProtocolActivityBuyReq(HP.code.HP_FULLY_ARMED_BUY_REQ_C_VALUE, playerId,
					msg.getCfgId(), msg.getCount());
			if (result.isFail()) {
				sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
				return false;
			}
		}
		return true;
	}

	@ProtocolHandler(code = HP.code.HP_FULLY_ARMED_SEARCH_REQ_C_VALUE)
	public boolean onSearch(HawkProtocol protocol, String playerId) {
		FullyArmedActivity activity = getActivity(ActivityType.FULLY_ARMED_ACTIVITY);
		if (null != activity) {
			Result<?> result = activity.onProtocolActivitySearchReq(HP.code.HP_FULLY_ARMED_BUY_REQ_C_VALUE, playerId);
			if (result.isFail()) {
				sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
				return false;
			}
		}
		return true;
	}
}

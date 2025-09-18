package com.hawk.activity.type.impl.firstRecharge;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;

/**
 * 首充
 * @author golden
 *
 */
public class FirstRechargeActivityHandler extends ActivityProtocolHandler {

	/**
	 * 领取首充奖励
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GET_FIRSTRECHARGE_REWARD_REQ_VALUE)
	public boolean onGetReward(HawkProtocol protocol, String playerId) {
		FirstRechargeActivity activity = getActivity(ActivityType.FIRST_RECHARGE_ACTIVITY);
		activity.pushReward(playerId);
		responseSuccess(playerId, protocol.getType());
		return true;
	}
}

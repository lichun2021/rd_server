package com.hawk.activity.type.impl.loginsign;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;

/**
 * 登录签到活动网络消息接收句柄
 * @author PhilChen
 *
 */
public class LoginSignActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 领取（每日签到）活动项奖励
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.TAKE_LOGIN_SIGN_REWARD_C_VALUE)
	public boolean onActivityReward(HawkProtocol protocol, String playerId) {
		LoginSignActivity activity = getActivity(ActivityType.LOGIN_SIGN_ACTIVITY);
		Result<?> result = activity.takeRewards(playerId);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return false;
		}
		
		responseSuccess(playerId, HP.code.TAKE_LOGIN_SIGN_REWARD_C_VALUE);
		return true;
	}
}

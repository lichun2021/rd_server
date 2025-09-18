package com.hawk.activity.type.impl.loginfund;


import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;

/**
 * 登录基金活动网络消息接收句柄
 * 
 * @author Jesse
 *
 */
public class LoginFundActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 购买登录基金
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.LOGIN_FUND_BUY_VALUE)
	public boolean onGrowfundBuy(HawkProtocol protocol, String playerId) {
		LoginFundActivity activity = getActivity(ActivityType.LOGIN_FUND_ACTIVITY);
		Result<?> result = activity.buyLoginfund(playerId);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return false;
		}
		responseSuccess(playerId, protocol.getType());
		return true;
	}
	
	/**
	 * 进入活动页签
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.LOGIN_FUND_ENTER_VALUE)
	public boolean onLoginfundEnter(HawkProtocol protocol, String playerId) {
		LoginFundActivity activity = getActivity(ActivityType.LOGIN_FUND_ACTIVITY);
		activity.enterActivity(playerId);
		return true;
	}

}

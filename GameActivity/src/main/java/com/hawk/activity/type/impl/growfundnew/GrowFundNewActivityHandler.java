package com.hawk.activity.type.impl.growfundnew;


import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;

/**
 * 新成长基金活动
 * 
 * @author lating
 *
 */
public class GrowFundNewActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 购买成长基金
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GROW_FUND_NEW_BUY_VALUE)
	public boolean onGrowfundBuy(HawkProtocol protocol, String playerId) {
		GrowFundNewActivity activity = getActivity(ActivityType.GROW_FUND_NEW_ACTIVITY);
		Result<?> result = activity.buyGrowfund(playerId);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return false;
		}
		
		responseSuccess(playerId, protocol.getType());
		return true;
	}
	
	/**
	 * 一键领取奖励
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GROW_FUND_NEW_ONEKEY_REWARD_VALUE)
	public boolean onOnekeyReward(HawkProtocol protocol, String playerId) {
		GrowFundNewActivity activity = getActivity(ActivityType.GROW_FUND_NEW_ACTIVITY);
		Result<?> result = activity.onekeyReward(playerId);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return false;
		}
		
		responseSuccess(playerId, protocol.getType());
		return true;
	}

}

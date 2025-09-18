package com.hawk.activity.type.impl.medalFund;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.MedalFundGetRewardReq;

/**勋章基金活动消息处理
 * @author Winder
 */
public class MedalFundHandler extends ActivityProtocolHandler {
	//勋章基金领取奖励
	@ProtocolHandler(code = HP.code.MEDAL_FUND_GET_REWARD_REQ_VALUE)
	public void getMedalFundReward(HawkProtocol protocol, String playerId){
		MedalFundGetRewardReq req =  protocol.parseProtocol(MedalFundGetRewardReq.getDefaultInstance());
		MedalFundActivity activity = getActivity(ActivityType.MEDAL_FUND_ACTIVITY);
		Result<?> result = activity.getMedalFundReward(playerId, req.getId(), protocol.getType());
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
}

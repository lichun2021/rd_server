package com.hawk.activity.type.impl.supersoldierInvest;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.SupersoldierInvestGetRewardReq;
import com.hawk.game.protocol.HP;

/**勋章基金活动消息处理
 * @author Winder
 */
public class SupersoldierInvestHandler extends ActivityProtocolHandler {
	//勋章基金领取奖励
	@ProtocolHandler(code = HP.code.SUPERSOLDIER_INVEST_REWARD_REQ_VALUE)
	public void getMedalFundReward(HawkProtocol protocol, String playerId){
		SupersoldierInvestGetRewardReq req =  protocol.parseProtocol(SupersoldierInvestGetRewardReq.getDefaultInstance());
		SupersoldierInvestActivity activity = getActivity(ActivityType.SUPERSOLDIER_INVEST_ACTIVITY);
		Result<?> result = activity.getSupersoldierReward(playerId, req.getId(), protocol.getType());
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
}

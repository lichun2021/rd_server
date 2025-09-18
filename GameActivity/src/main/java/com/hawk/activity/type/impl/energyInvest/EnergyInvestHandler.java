package com.hawk.activity.type.impl.energyInvest;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.EnergyInvestGetRewardReq;
import com.hawk.game.protocol.Activity.MedalFundGetRewardReq;

/**勋章基金活动消息处理
 * @author Winder
 */
public class EnergyInvestHandler extends ActivityProtocolHandler {
	//勋章基金领取奖励
	@ProtocolHandler(code = HP.code.ENERGY_INVEST_REWARD_REQ_VALUE)
	public void getMedalFundReward(HawkProtocol protocol, String playerId){
		EnergyInvestGetRewardReq req =  protocol.parseProtocol(EnergyInvestGetRewardReq.getDefaultInstance());
		EnergyInvestActivity activity = getActivity(ActivityType.ENERGY_INVEST_ACTIVITY);
		Result<?> result = activity.getMedalFundReward(playerId, req.getId(), protocol.getType());
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
}

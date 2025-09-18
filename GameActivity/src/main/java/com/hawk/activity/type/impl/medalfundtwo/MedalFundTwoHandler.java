package com.hawk.activity.type.impl.medalfundtwo;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.MedalFundTwoGetRewardReq;

/**
 * 新版勋章基金活动
 * 
 * @author lating
 */
public class MedalFundTwoHandler extends ActivityProtocolHandler {
	
	//勋章基金领取奖励
	@ProtocolHandler(code = HP.code.MEDAL_FUND_TWO_GET_REWARD_REQ_VALUE)
	public void getMedalFundReward(HawkProtocol protocol, String playerId){
		MedalFundTwoGetRewardReq req =  protocol.parseProtocol(MedalFundTwoGetRewardReq.getDefaultInstance());
		MedalFundTwoActivity activity = getActivity(ActivityType.MEDAL_FUND_TWO_ACTIVITY);
		Result<?> result = activity.getMedalFundReward(playerId, req.getId(), protocol.getType());
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
}

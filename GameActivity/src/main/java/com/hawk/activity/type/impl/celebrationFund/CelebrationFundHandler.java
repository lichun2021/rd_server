package com.hawk.activity.type.impl.celebrationFund;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.CelebrationFundBuyScoreReq;
import com.hawk.game.protocol.HP;

/**
 * 周年庆庆典基金
 * LiJiaLiang，FangWeijie
 */
public class CelebrationFundHandler extends ActivityProtocolHandler {
	
	@ProtocolHandler(code = HP.code2.CELEBRATION_FUND_SCORE_REQ_VALUE)
	public void  buyScore(HawkProtocol protocol, String playerId){
		CelebrationFundActivity activity = this.getActivity(ActivityType.CELEBRATION_FUND_ACTIVITY);
		if(activity == null){
			return;
		}
		
		CelebrationFundBuyScoreReq req = protocol.parseProtocol(CelebrationFundBuyScoreReq.getDefaultInstance());
		int result = activity.buyScore(playerId, req.getScore());
		if (result != 0) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
		}				
	}
}
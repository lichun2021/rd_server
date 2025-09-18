package com.hawk.activity.type.impl.beauty.contest;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.BeautyContestBuyFlowerReq;
import com.hawk.game.protocol.HP;

/**
 * 选美初赛
 * 
 * @author lating
 *
 */
public class BeautyContestHandler extends ActivityProtocolHandler {
	
	
	@ProtocolHandler(code = HP.code2.BEAUTY_CONTEST_BUY_FLOWER_REQ_VALUE)
	public void openCardReq(HawkProtocol protocol, String playerId){
		BeautyContestBuyFlowerReq req = protocol.parseProtocol(BeautyContestBuyFlowerReq.getDefaultInstance());
		BeautyContestActivity activity = this.getActivity(ActivityType.BEAUTY_CONTEST_ACTIVITY);
		Result<?> result = activity.buyFlower(playerId, req.getCount());
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		} else {
			responseSuccess(playerId, protocol.getType());
		}
	}
	
}
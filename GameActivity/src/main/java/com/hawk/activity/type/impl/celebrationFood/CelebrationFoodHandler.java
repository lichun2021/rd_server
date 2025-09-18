package com.hawk.activity.type.impl.celebrationFood;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.MakeCelebrationFoodReq;
import com.hawk.game.protocol.HP;

/**
 * 庆典美食
 * hf
 */
public class CelebrationFoodHandler extends ActivityProtocolHandler {
	
	@ProtocolHandler(code = HP.code.CELEBRATION_FOOD_MAKE_REQ_VALUE)
	public void  getCakeShareInfo(HawkProtocol protocol, String playerId){
		CelebrationFoodActivity activity = this.getActivity(ActivityType.CELEBRATION_FOOD_ACTIVITY);
		if(activity == null){
			return;
		}
		MakeCelebrationFoodReq req = protocol.parseProtocol(MakeCelebrationFoodReq.getDefaultInstance());
		Result<?> result = activity.celebrationFoodMake(playerId, req.getLevel());
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}				
	}
	
	
}
package com.hawk.activity.type.impl.heroBack;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.HeroBackBuyReq;
import com.hawk.game.protocol.HP;

public class HeroBackHandler extends ActivityProtocolHandler {
	
	//@ProtocolHandler(code = HP.code.HERO_BACK_BUY_C_VALUE)
	public void onPlayerBuySupplyBox(HawkProtocol protocol, String playerId){
		HeroBackActivity activity = getActivity(ActivityType.HERO_BACK);
		HeroBackBuyReq req = protocol.parseProtocol(HeroBackBuyReq.getDefaultInstance());
		int chestId = req.getId();
		int count = req.getCount();
		if(count <= 0 || chestId <= 0){
			HawkLog.errPrintln("HeroBackBuyReq error, chestId:{}, countId:{}", chestId, count);
			return;
		}
		Result<?> result = activity.onPlayerBuyChest(chestId, count, playerId);
		if(result != null && result.isFail()){
			sendErrorAndBreak(playerId, HP.code.HERO_BACK_BUY_C_VALUE, result.getStatus());
		}
	}
}

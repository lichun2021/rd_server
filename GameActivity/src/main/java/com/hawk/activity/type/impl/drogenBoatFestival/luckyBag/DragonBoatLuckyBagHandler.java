package com.hawk.activity.type.impl.drogenBoatFestival.luckyBag;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.DragonBoatLuckyBagOpenReq;
import com.hawk.game.protocol.HP;

/**
 * 端午兑换
 * 
 * @author che
 *
 */
public class DragonBoatLuckyBagHandler extends ActivityProtocolHandler {
	
	@ProtocolHandler(code = HP.code.DRAGON_BOAT_LUCKY_BAG_OPEN_REQ_VALUE)
	public void dragonBoatCelebrationInfo(HawkProtocol hawkProtocol, String playerId){
		DragonBoatLuckyBagActivity activity = this.getActivity(ActivityType.DRAGON_BOAT_LUCKY_BAG_ACTIVITY);
		if(activity == null){
			return;
		}
		DragonBoatLuckyBagOpenReq req = hawkProtocol.parseProtocol(DragonBoatLuckyBagOpenReq.getDefaultInstance());
		int openCount = req.getOpenCount();
		activity.openLuckBag(playerId, openCount, hawkProtocol.getType());
	}
	
	

	
	
}
package com.hawk.activity.type.impl.drogenBoatFestival.gift;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;

/**
 * 端午龙船送礼
 * 
 * @author che
 *
 */
public class DragonBoatGiftHandler extends ActivityProtocolHandler {
	
	@ProtocolHandler(code = HP.code.DRAGON_BOAT_GIFT_INFO_REQ_VALUE)
	public void  getDragonBoatInfo(HawkProtocol hawkProtocol, String playerId){
		DragonBoatGiftActivity activity = this.getActivity(ActivityType.DRAGON_BOAT_GIFT_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.syncActivityDataInfo(playerId);
	}
	
	
	@ProtocolHandler(code = HP.code.DRAGON_BOAT_LOCATION_REQ_VALUE)
	public void getDragonLocation(HawkProtocol hawkProtocol, String playerId){
		DragonBoatGiftActivity activity = this.getActivity(ActivityType.DRAGON_BOAT_GIFT_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.getDragonBoatPos(playerId);
	}
	
	
	
	
	
}
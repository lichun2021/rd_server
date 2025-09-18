package com.hawk.activity.type.impl.cakeShare;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;

/**
 * 蛋糕同享
 * hf
 */
public class CakeShareHandler extends ActivityProtocolHandler {
	
	@ProtocolHandler(code = HP.code.CAKE_SHARE_INFO_REQ_VALUE)
	public void  getCakeShareInfo(HawkProtocol hawkProtocol, String playerId){
		CakeShareActivity activity = this.getActivity(ActivityType.CAKE_SHARE_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.syncActivityDataInfo(playerId);
	}
	
	
	@ProtocolHandler(code = HP.code.CAKE_SHARE_LOCATION_REQ_VALUE)
	public void getCakeLocation(HawkProtocol hawkProtocol, String playerId){
		CakeShareActivity activity = this.getActivity(ActivityType.CAKE_SHARE_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.getCakeSharePos(playerId);
	}
	
	
	
	
	
}
package com.hawk.activity.type.impl.hotBloodWar;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.PBFirstRecoverSetReq;
import com.hawk.game.protocol.Activity.PBRecoverSpeedReq;
import com.hawk.game.protocol.HP;

/**
 * 
 * @author che
 *
 */
public class HotBloodWarHandler extends ActivityProtocolHandler {
	
	
	@ProtocolHandler(code = HP.code2.HOT_BLOOD_WAR_INFO_C_VALUE)
	public void chooseId(HawkProtocol hawkProtocol, String playerId){
		HotBloodWarActivity activity = this.getActivity(ActivityType.HOT_BLOOD_WAR_378);
		if(activity == null){
			return;
		}
		activity.getActivityData(playerId);
	}

	
	
	@ProtocolHandler(code = HP.code2.HOT_BLOOD_WAR_CURE_ACHIEVE_C_VALUE)
	public void achieveHealthArmy(HawkProtocol hawkProtocol, String playerId){
		HotBloodWarActivity activity = this.getActivity(ActivityType.HOT_BLOOD_WAR_378);
		if(activity == null){
			return;
		}
		activity.achieveCureArmy(playerId);
	}

	
	@ProtocolHandler(code = HP.code2.HOT_BLOOD_WAR_CURE_FIRST_C_VALUE)
	public void setFirstCureType(HawkProtocol hawkProtocol, String playerId){
		HotBloodWarActivity activity = this.getActivity(ActivityType.HOT_BLOOD_WAR_378);
		if(activity == null){
			return;
		}
		PBFirstRecoverSetReq req = hawkProtocol.parseProtocol(
				PBFirstRecoverSetReq.getDefaultInstance());
		activity.setFirstCureType(playerId,req.getArmyType());
	}

	@ProtocolHandler(code = HP.code2.HOT_BLOOD_WAR_CURE_SPEED_C_VALUE)
	public void useSpeedItem(HawkProtocol hawkProtocol, String playerId){
		HotBloodWarActivity activity = this.getActivity(ActivityType.HOT_BLOOD_WAR_378);
		if(activity == null){
			return;
		}
		PBRecoverSpeedReq req = hawkProtocol.parseProtocol(
				PBRecoverSpeedReq.getDefaultInstance());
		activity.itemSpeedUp(playerId, req.getSpeedItemList());
	}
	
	
	
}
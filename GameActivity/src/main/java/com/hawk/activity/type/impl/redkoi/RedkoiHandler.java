package com.hawk.activity.type.impl.redkoi;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;

public class RedkoiHandler  extends ActivityProtocolHandler{
	
	
	
	@ProtocolHandler(code = HP.code.REDKOI_MAKE_WISH_REQ_VALUE)
	public boolean makeWish(HawkProtocol protocol, String playerId){
		RedkoiActivity activity = getActivity(ActivityType.REDKOI_ACTIVITY);
		return activity.wishMake(playerId,protocol);
	}
	

	
	@ProtocolHandler(code = HP.code.REDKOI_AWARD_CHANGE_REQ_VALUE)
	public boolean chooseAward(HawkProtocol protocol, String playerId){
		RedkoiActivity activity = getActivity(ActivityType.REDKOI_ACTIVITY);
		boolean rlt = activity.chooseAward(playerId, protocol);
		if(rlt){
			responseSuccess(playerId, protocol.getType());
		}
		return rlt;
	}
	
	@ProtocolHandler(code = HP.code.REDKOI_INFO_SYNC_REQ_VALUE)
	public boolean redkoiActivityInfo(HawkProtocol protocol, String playerId){
		RedkoiActivity activity = getActivity(ActivityType.REDKOI_ACTIVITY);
		return activity.getActivityInfo(playerId);
	}
	
	
	@ProtocolHandler(code = HP.code.REDKOI_AWARD_RECORD_REQ_VALUE)
	public boolean redkoiAwardRecord(HawkProtocol protocol, String playerId){
		RedkoiActivity activity = getActivity(ActivityType.REDKOI_ACTIVITY);
		return activity.getKoiAwardRecord(playerId);
	}
	

}

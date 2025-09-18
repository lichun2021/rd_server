package com.hawk.activity.type.impl.dressCollectionTwo;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;

public class DressCollectionTwoHandler extends ActivityProtocolHandler{

	@ProtocolHandler(code = HP.code2.DRESS_COLLECTION_TWO_INFO_REQ_VALUE)
	public void getBlessPageInfoReq(HawkProtocol protocol, String playerId){
		DressCollectionTwoActivity activity = getActivity(ActivityType.DRESS_COLLECTION_ACTIVITY_TWO);
		if(null != activity && activity.isOpening(playerId)){
			activity.syncActivityDataInfo(playerId);
		}
	}
	
}

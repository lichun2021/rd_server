package com.hawk.activity.type.impl.powercollect;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;

public class PowerCollectHandler extends ActivityProtocolHandler {
	
	@ProtocolHandler(code = HP.code.POWER_COLLECT_INFO_C_VALUE)
	public void onPlayerReqInfo(HawkProtocol protocol, String playerId){
		PowerCollectActivity activity = getActivity(ActivityType.SUPER_POWER_LAB);
		if(activity != null){
			if(activity.isOpening(playerId)){
				activity.syncRankInfo(playerId);
			}
		}
	}
}

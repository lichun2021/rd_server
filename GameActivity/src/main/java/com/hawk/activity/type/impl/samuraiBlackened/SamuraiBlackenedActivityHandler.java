package com.hawk.activity.type.impl.samuraiBlackened;


import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.SamuraiBlackenedInfoResp;
import com.hawk.game.protocol.HP;

/**
 * 黑武士
 * @author jm
 */
public class SamuraiBlackenedActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 进入活动界面
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.SAMURAI_BLACKENED_INFO_REQ_VALUE)
	public boolean onEnterPage(HawkProtocol protocol, String playerId) {	
		SamuraiBlackenedActivity activity = getActivity(ActivityType.SAMURAI_BLACKENED_ACTIVITY);
		SamuraiBlackenedInfoResp.Builder builder = activity.genPageInfo(playerId);
		if(builder != null){
			sendProtocol(playerId, HawkProtocol.valueOf(HP.code.SAMURAI_BLACKENED_INFO_RESP_VALUE, builder));
		}
		return true;
	}

}

package com.hawk.activity.type.impl.coreplate;


import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;

/**
 * 雄心壮志
 * @author che
 *
 */
public class CoreplateActivityHandler extends ActivityProtocolHandler {

	/**
	 * 领宝箱
	 * @param playerId
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.COREPLATE_ACTIVITY_ACHIEVE_BOX_REQ_VALUE)
	public boolean onGetStageInfo(HawkProtocol protocol, String playerId) {
		CoreplateActivity activity = getActivity(ActivityType.COREPLATE_ACTIVITY);
		activity.achiveBox(playerId);
		return true;
	}
	
	
}

package com.hawk.activity.type.impl.exclusiveMomory;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;

/**
 * @author che
 *
 */
public class ExclusiveMemoryHandler extends ActivityProtocolHandler {
	
	
	@ProtocolHandler(code = HP.code2.EXCLUSIVE_MEMORY_OPEN_REQ_VALUE)
	public void chooseHero(HawkProtocol hawkProtocol, String playerId){
		ExclusiveMemoryActivity activity = this.getActivity(ActivityType.EXCLUSIVE_MEMORY_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.openExclusiveMemory(playerId);
	}
	
}
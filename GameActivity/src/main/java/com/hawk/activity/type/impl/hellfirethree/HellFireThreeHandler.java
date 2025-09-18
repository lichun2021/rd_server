package com.hawk.activity.type.impl.hellfirethree;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.HellFireThreeReceiveReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

public class HellFireThreeHandler extends ActivityProtocolHandler {
	@ProtocolHandler(code = HP.code.HELL_FIRE_THREE_INFO_C_VALUE)
	public void onHellFireInfo(HawkProtocol hawkProtocol, String playerId) {
		HellFireThreeActivity hellFireActivity = this.getActivity(ActivityType.HELL_FIRE_THREE_ACTIVITY);
		hellFireActivity.syncActivityDataInfo(playerId);
	}
	@ProtocolHandler(code = HP.code.HELL_FIRE_THREE_RECEIVE_REQ_VALUE) 
	public void onHellFireReceive(HawkProtocol hawkProtocol, String playerId){
		HellFireThreeActivity hellFireActivity = this.getActivity(ActivityType.HELL_FIRE_THREE_ACTIVITY);
		HellFireThreeReceiveReq cparam = hawkProtocol.parseProtocol(HellFireThreeReceiveReq.getDefaultInstance()); 
		int result = hellFireActivity.receive(playerId, cparam.getTargetId());
		
		if (result == Status.SysError.SUCCESS_OK_VALUE) {
			this.responseSuccess(playerId, hawkProtocol.getType());
		} else {
			this.sendErrorAndBreak(playerId, hawkProtocol.getType(), result);
		}
	}
}
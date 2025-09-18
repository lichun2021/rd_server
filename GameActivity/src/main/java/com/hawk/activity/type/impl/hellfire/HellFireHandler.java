package com.hawk.activity.type.impl.hellfire;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.HellFireOneReceiveReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

public class HellFireHandler extends ActivityProtocolHandler {
	@ProtocolHandler(code = HP.code.HELL_FIRE_INFO_C_VALUE)
	public void onHellFireInfo(HawkProtocol hawkProtocol, String playerId) {
		HellFireActivity hellFireActivity = this.getActivity(ActivityType.HELL_FIRE_ACTIVITY);
		hellFireActivity.syncActivityDataInfo(playerId);
	}
	
	@ProtocolHandler(code = HP.code.HELL_FIRE_ONE_RECEIVE_REQ_VALUE) 
	public void onHellFireReceive(HawkProtocol hawkProtocol, String playerId){
		HellFireActivity hellFireActivity = this.getActivity(ActivityType.HELL_FIRE_ACTIVITY);
		HellFireOneReceiveReq cparam = hawkProtocol.parseProtocol(HellFireOneReceiveReq.getDefaultInstance()); 
		int result = hellFireActivity.receive(playerId, cparam.getTargetId());
		
		if (result == Status.SysError.SUCCESS_OK_VALUE) {
			this.responseSuccess(playerId, hawkProtocol.getType());
		} else {
			this.sendErrorAndBreak(playerId, hawkProtocol.getType(), result);
		}
	}
	
	@ProtocolHandler(code = HP.code.HELL_FIRE_RANK_REQ_VALUE)
	public void onHellFireRankReq(HawkProtocol protocol, String playerId){
		HellFireActivity hellFireActivity = this.getActivity(ActivityType.HELL_FIRE_ACTIVITY);
		if (hellFireActivity.isAllowOprate(playerId)) {
			hellFireActivity.pushRankInfo(playerId);
		}
	}
}
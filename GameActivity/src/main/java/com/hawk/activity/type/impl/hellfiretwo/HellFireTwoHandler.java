package com.hawk.activity.type.impl.hellfiretwo;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.HellFireTwoReceiveReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

public class HellFireTwoHandler extends ActivityProtocolHandler {
	@ProtocolHandler(code = HP.code.HELL_FIRE_TWO_INFO_C_VALUE)
	public void onHellFireInfo(HawkProtocol hawkProtocol, String playerId) {
		HellFireTwoActivity hellFireActivity = this.getActivity(ActivityType.HELL_FIRE_TWO_ACTIVITY);
		hellFireActivity.syncActivityDataInfo(playerId);
	}
	
	@ProtocolHandler(code = HP.code.HELL_FIRE_TWO_RECEIVE_REQ_VALUE) 
	public void onHellFireReceive(HawkProtocol hawkProtocol, String playerId){
		HellFireTwoActivity hellFireActivity = this.getActivity(ActivityType.HELL_FIRE_TWO_ACTIVITY);
		HellFireTwoReceiveReq cparam = hawkProtocol.parseProtocol(HellFireTwoReceiveReq.getDefaultInstance()); 
		int result = hellFireActivity.receive(playerId, cparam.getTargetId());
		
		if (result == Status.SysError.SUCCESS_OK_VALUE) {
			this.responseSuccess(playerId, hawkProtocol.getType());
		} else {
			this.sendErrorAndBreak(playerId, hawkProtocol.getType(), result);
		}
	}
	
	@ProtocolHandler(code = HP.code.HELL_FIRE_TWO_RANK_REQ_VALUE)
	public void onHellFireRankReq(HawkProtocol protocol, String playerId){
		HellFireTwoActivity hellFireActivity = this.getActivity(ActivityType.HELL_FIRE_TWO_ACTIVITY);
		if (hellFireActivity.isAllowOprate(playerId)) {
			hellFireActivity.pushRankInfo(playerId);
		}
	}
}
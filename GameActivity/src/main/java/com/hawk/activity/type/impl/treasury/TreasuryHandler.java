package com.hawk.activity.type.impl.treasury;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.TreasuryReceiveReq;
import com.hawk.game.protocol.Activity.TreasuryReceiveResp;
import com.hawk.game.protocol.HP;

public class TreasuryHandler extends ActivityProtocolHandler {
	@ProtocolHandler(code = HP.code.TREASURY_INFO_REQ_VALUE)
	public void onTreasuryInfoReq(HawkProtocol protocol, String playerId) {
		TreasuryActivity treasuryActivity = this.getActivity(ActivityType.TREASURY);
		treasuryActivity.syncActivityDataInfo(playerId);
	}
	
	@ProtocolHandler(code = HP.code.TREASURY_RECEIVE_REQ_VALUE)
	public void onTreasuryReceiveReq(HawkProtocol protocol, String playerId) {
		TreasuryReceiveReq cparam = protocol.parseProtocol(TreasuryReceiveReq.getDefaultInstance()); 
		TreasuryActivity treasuryActivity = this.getActivity(ActivityType.TREASURY);
		int result = treasuryActivity.onReceiveReward(playerId, cparam.getTreasuryId());
		
		TreasuryReceiveResp.Builder sbuilder = TreasuryReceiveResp.newBuilder();
		sbuilder.setTreasuryId(cparam.getTreasuryId());
		sbuilder.setResult(result);
		
		this.sendProtocol(playerId, HawkProtocol.valueOf(HP.code.TREASURY_RECEIVE_RESP_VALUE, sbuilder));
	} 
	
}

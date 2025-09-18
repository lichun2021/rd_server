package com.hawk.activity.type.impl.allyBeatBack;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.AllyBeatBackExchangeReq;
import com.hawk.game.protocol.HP;

public class AllyBeatBackHanlder extends ActivityProtocolHandler {
	@ProtocolHandler(code = HP.code.ALLY_BEAT_BACK_EXCHANGE_REQ_VALUE)
	public void onExchange(HawkProtocol protocol, String playerId) {
		AllyBeatBackExchangeReq cparam = protocol.parseProtocol(AllyBeatBackExchangeReq.getDefaultInstance());
		AllyBeatBackActivity activity = this.getActivity(ActivityType.ALLY_BEAT_BACK);
		if (activity.isAllowOprate(playerId)) {			
			Result<Void> result = activity.exchage(playerId, cparam.getExchangeId(), cparam.getNum());
			if (result.isOk()) {
				this.responseSuccess(playerId, protocol.getType());
				activity.syncActivityDataInfo(playerId);
			} else {
				this.sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			}
		}				
	}
	
	@ProtocolHandler(code = HP.code.ALLY_BEAT_BACK_INFO_REQ_VALUE)
	public void onInfoReq(HawkProtocol protocol, String playerId) {
		AllyBeatBackActivity activity = this.getActivity(ActivityType.ALLY_BEAT_BACK);
		if (activity.isAllowOprate(playerId)) {
			activity.syncActivityDataInfo(playerId);
		}
	}
	
	@ProtocolHandler(code = HP.code.ALLY_BEAT_BACK_RECEIVE_REQ_VALUE) 
	public void onReceiveReq(HawkProtocol protocol, String playerId) {
		AllyBeatBackActivity activity = this.getActivity(ActivityType.ALLY_BEAT_BACK);
		if (activity.isAllowOprate(playerId)) {
			Result<Void> result = activity.receiveReward(playerId);
			if (result.isOk()) {
				this.responseSuccess(playerId, protocol.getType());
			} else {
				this.sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			}
		}
	}
}

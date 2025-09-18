package com.hawk.activity.type.impl.redEnvelope;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.RedEnvelopeRecieve;
import com.hawk.game.protocol.HP;

public class RedEnvelopeHandler extends ActivityProtocolHandler {
	
	@ProtocolHandler(code = HP.code.RED_ENVELOPE_INFO_C_VALUE)
	public void infoReq(HawkProtocol protocol, String playerId){
		RedEnvelopeActivity activity = getActivity(ActivityType.RED_ENVELOPE_ACTIVITY);
		activity.syncActivityInfo(playerId);
	}
	
	/***
	 * 领取红包
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.RED_ENVELOPE_RECIEVE_VALUE)
	public void recieveRedEnvelope(HawkProtocol protocol, String playerId){
		RedEnvelopeActivity activity = getActivity(ActivityType.RED_ENVELOPE_ACTIVITY);
		RedEnvelopeRecieve recieve = protocol.parseProtocol(RedEnvelopeRecieve.getDefaultInstance());
		int stageId = recieve.getId();
		if(stageId == 0){
			return;
		}
		if(activity != null){
			Result<?> result = activity.onPlayerRecieveRedEnvelope(playerId, stageId);
			if(result != null){
				sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			}
		}
	}
	
	@ProtocolHandler(code = HP.code.RED_ENVELOPE_HISTORY_INFO_C_VALUE)
	public void onPlayerReqHistory(HawkProtocol protocol, String playerId){
		RedEnvelopeActivity activity = getActivity(ActivityType.RED_ENVELOPE_ACTIVITY);
		activity.pushPlayerRedEnvelopeHistory(playerId);
	}
	
	@ProtocolHandler(code = HP.code.RED_ENVELOPE_RECIEVE_DETAIL_VALUE)
	public void onPlayerReqRedEnvelopeDetail(HawkProtocol protocol, String playerId){
		RedEnvelopeRecieve req = protocol.parseProtocol(RedEnvelopeRecieve.getDefaultInstance());
		RedEnvelopeActivity activity = getActivity(ActivityType.RED_ENVELOPE_ACTIVITY);
		int stageID = req.getId();
		if(stageID == 0){
			return;
		}
		if(activity != null){
			activity.playerReqRedEnvelopeDetail(stageID, playerId);
		}
	}
}

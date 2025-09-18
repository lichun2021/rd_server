package com.hawk.activity.type.impl.redPackage;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.RedPackageRecieveReq;
import com.hawk.game.protocol.HP;

public class RedPackageHandler extends ActivityProtocolHandler {
	
	@ProtocolHandler(code = HP.code.RED_PACKAGE_INFO_REQ_VALUE)
	public void infoReq(HawkProtocol protocol, String playerId){
		RedPackageActivity activity = getActivity(ActivityType.RED_PACKAGE_ACTIVITY);
		activity.syncActivityDataInfo(playerId);
	}
	
	/***
	 * 领取红包
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.RED_PACKAGE_RECIEVE_REQ_VALUE)
	public void recieveRedEnvelope(HawkProtocol protocol, String playerId){
		RedPackageActivity activity = getActivity(ActivityType.RED_PACKAGE_ACTIVITY);
		RedPackageRecieveReq recieve = protocol.parseProtocol(RedPackageRecieveReq.getDefaultInstance());
		int stageId = recieve.getStageId();
		if(stageId == 0){
			return;
		}
		int score = recieve.getScore();
		activity.onPlayerRecieveRedPackage(playerId, stageId, score);
	}
	
	@ProtocolHandler(code = HP.code.RED_PACKAGE_RECIEVE_RECORD_REQ_VALUE)
	public void onPlayerReqHistory(HawkProtocol protocol, String playerId){
		RedPackageActivity activity = getActivity(ActivityType.RED_ENVELOPE_ACTIVITY);
		activity.getRedPackageRecords(playerId);
	}
	

}

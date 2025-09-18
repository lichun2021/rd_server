package com.hawk.activity.type.impl.overlordBlessing;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;

public class OverlordBlessingHandler extends ActivityProtocolHandler{

	//前往膜拜
//	@ProtocolHandler(code=HP.code.OVERLORD_BLESS_GO_REQ_VALUE)
//	public void setRewardItemReq(HawkProtocol protocol, String playerId){
//		OverlordBlessingActivity activity = getActivity(ActivityType.OVERLORD_BLESS_ACTIVITY);
//		if(null != activity && activity.isOpening(playerId)){
//			Result<?> result = activity.goToBlessingOverlord(playerId);
//			if(result.isFail()){
//				sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
//			}else{
//				responseSuccess(playerId, protocol.getType());
//			}
//		}
//	}
	
	 //领取膜拜奖励
	@ProtocolHandler(code=HP.code.OVERLORD_BLESS_REWARD_REQ_VALUE)
	public void receiveBlessRewardReq(HawkProtocol protocol, String playerId){
		OverlordBlessingActivity activity = getActivity(ActivityType.OVERLORD_BLESS_ACTIVITY);
		if(null != activity && activity.isOpening(playerId)){
			Result<?> result = activity.receiveBlessingReward(playerId);
			if(result.isFail()){
				sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			}else{
				responseSuccess(playerId, protocol.getType());
			}
		}
	}
	
	//领取膜拜分享奖励
	@ProtocolHandler(code=HP.code.OVERLORD_BLESS_SHARE_REWARD_REQ_VALUE)
	public void receiveBlessShareRewardReq(HawkProtocol protocol, String playerId){
		OverlordBlessingActivity activity = getActivity(ActivityType.OVERLORD_BLESS_ACTIVITY);
		if(null != activity && activity.isOpening(playerId)){
			Result<?> result = activity.receiveBlessingShareReward(playerId);
			if(result.isFail()){
				sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			}else{
				responseSuccess(playerId, protocol.getType());
			}
		}
	}
	
	
	//领取膜拜分享奖励
	@ProtocolHandler(code=HP.code.OVERLORD_BLESS_INFO_REQ_VALUE)
	public void getBlessPageInfoReq(HawkProtocol protocol, String playerId){
		OverlordBlessingActivity activity = getActivity(ActivityType.OVERLORD_BLESS_ACTIVITY);
		if(null != activity && activity.isOpening(playerId)){
			activity.syncActivityDataInfo(playerId);
		}
	}
	
	
	
}

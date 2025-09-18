package com.hawk.activity.type.impl.alliancecelebrate;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.DragonBoatCeletrationMakeGiftReq;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

/**
 * 联盟欢庆消息处理
 * @author hf
 */
public class AllianceCelebrateHandler extends ActivityProtocolHandler {

	/**
	 * 同步界面
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.ALLIANCE_CELEBRATE_INFO_REQ_VALUE)
	public void allianceCelebratePageInfo(HawkProtocol protocol, String playerId){
		AllianceCelebrateActivity activity = this.getActivity(ActivityType.ALLIANCE_CELEBRATE_ACTIVITY);
		if(activity == null){
			return;
		}
		/**同步界面*/
		activity.syncActivityDataInfo(playerId);
	}
	/**
	 * 捐献
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.ALLIANCE_CELEBRATE_DONATION_REQ_VALUE)
	public void allianceCelebrateDonate(HawkProtocol protocol, String playerId){
		Activity.AllianceCelebrateDonateReq req = protocol.parseProtocol(Activity.AllianceCelebrateDonateReq.getDefaultInstance());
		AllianceCelebrateActivity activity = this.getActivity(ActivityType.ALLIANCE_CELEBRATE_ACTIVITY);
		if(activity == null){
			return;
		}
		/**捐献个数*/
		int num = req.getNum();
		activity.donationAllianceExpReq(playerId,num, protocol.getType());
	}

	@ProtocolHandler(code = HP.code.ALLIANCE_CELEBRATE_REWARD_REQ_VALUE)
	public void allianceCelebrateGetReward(HawkProtocol protocol, String playerId){
		AllianceCelebrateActivity activity = this.getActivity(ActivityType.ALLIANCE_CELEBRATE_ACTIVITY);
		if(activity == null){
			return;
		}
		Activity.AllianceCelebrateRewardReq req = protocol.parseProtocol(Activity.AllianceCelebrateRewardReq.getDefaultInstance());
		Result<?> result = activity.receiveAllianceCelebrateReward(playerId, req);
		if(result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return;
		}else{
			responseSuccess(playerId, protocol.getType());
		}
	}

	
	
}
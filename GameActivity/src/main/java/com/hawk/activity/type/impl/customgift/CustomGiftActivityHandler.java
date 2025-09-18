package com.hawk.activity.type.impl.customgift;

import com.hawk.game.protocol.Activity;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.CustomRewardSelectReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

/**
 * 定制礼包活动
 * 
 * @author lating
 *
 */
public class CustomGiftActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 请求礼包信息
	 * 
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.CUSTOM_GIFT_INFO_REQ_VALUE)
	public void onGetCustomGiftInfo(HawkProtocol hawkProtocol, String playerId) {
		CustomGiftActivity activity = this.getActivity(ActivityType.CUSTOM_GIFT_ACTIVITY);
		int result = activity.syncCustomGiftInfo(playerId);
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			this.sendErrorAndBreak(playerId, hawkProtocol.getType(), result);
		}
	}
	
	/**
	 * 定制礼包（确认）选择奖励请求
	 * 
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.CUSTOM_REWARD_SELECT_REQ_VALUE)
	public void onSelectCustomReward(HawkProtocol hawkProtocol, String playerId) {
		CustomRewardSelectReq req = hawkProtocol.parseProtocol(CustomRewardSelectReq.getDefaultInstance());
		CustomGiftActivity activity = this.getActivity(ActivityType.CUSTOM_GIFT_ACTIVITY);
		int result = activity.selectCustomReward(playerId, req.getGiftId(), req.getRewardIdList());
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			this.sendErrorAndBreak(playerId, hawkProtocol.getType(), result);
		}
	}


	@ProtocolHandler(code = HP.code2.CUSTOM_REWARD_GET_REQ_VALUE)
	public void onGetCustomReward(HawkProtocol hawkProtocol, String playerId) {
		Activity.CustomRewardGetReq req = hawkProtocol.parseProtocol(Activity.CustomRewardGetReq.getDefaultInstance());
		CustomGiftActivity activity = this.getActivity(ActivityType.CUSTOM_GIFT_ACTIVITY);
		int result = activity.onGetCustomReward(playerId, req.getGiftId());
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			this.sendErrorAndBreak(playerId, hawkProtocol.getType(), result);
		}
	}
	
}
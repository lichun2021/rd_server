package com.hawk.activity.type.impl.pioneergift;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.PioneerGiftSelectReq;

/**
 * 先锋豪礼活动
 * 
 * @author lating
 *
 */
public class PioneerGiftActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 请求活动信息
	 * 
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.PIONEER_GIFT_INFO_REQ_VALUE)
	public void onGetActivityInfo(HawkProtocol hawkProtocol, String playerId) {
		PioneerGiftActivity activity = this.getActivity(ActivityType.PIONEER_GIFT_ACTIVITY);
		int result = activity.syncPioneerGiftInfo(playerId);
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			this.sendErrorAndBreak(playerId, hawkProtocol.getType(), result);
		}
	}
	
	/**
	 * 领取免费礼包
	 * 
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.PIONEER_GIFT_FREE_REQ_VALUE)
	public void onTakeFreeReward(HawkProtocol hawkProtocol, String playerId) {
		PioneerGiftActivity activity = this.getActivity(ActivityType.PIONEER_GIFT_ACTIVITY);
		int result = activity.receiveFreeGift(playerId);
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			this.sendErrorAndBreak(playerId, hawkProtocol.getType(), result);
		}
	}
	
	/**
	 * 选择档次礼包
	 * 
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.PIONEER_GIFT_SELECT_REQ_VALUE)
	public void onSelectPioneerGift(HawkProtocol hawkProtocol, String playerId) {
		PioneerGiftSelectReq req = hawkProtocol.parseProtocol(PioneerGiftSelectReq.getDefaultInstance());
		PioneerGiftActivity activity = this.getActivity(ActivityType.PIONEER_GIFT_ACTIVITY);
		int result = activity.selectPioneerGift(playerId, req.getType(), req.getGiftId());
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			this.sendErrorAndBreak(playerId, hawkProtocol.getType(), result);
		}
	}
	
//	/**
//	 * 领取累计购买奖励
//	 * 
//	 * @param hawkProtocol
//	 * @param playerId
//	 */
//	@ProtocolHandler(code = HP.code.PIONEER_GIFT_ACC_REWARD_REQ_VALUE)
//	public void onGetAccReward(HawkProtocol hawkProtocol, String playerId) {
//		PioneerGiftAccRewardReq req = hawkProtocol.parseProtocol(PioneerGiftAccRewardReq.getDefaultInstance());
//		PioneerGiftActivity activity = this.getActivity(ActivityType.PIONEER_GIFT_ACTIVITY);
//		int result = activity.receiveAccReward(playerId, req.getDay());
//		if (result != Status.SysError.SUCCESS_OK_VALUE) {
//			this.sendErrorAndBreak(playerId, hawkProtocol.getType(), result);
//		}
//	}
	
}
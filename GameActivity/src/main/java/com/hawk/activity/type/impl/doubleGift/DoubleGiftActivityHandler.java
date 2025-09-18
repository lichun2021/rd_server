package com.hawk.activity.type.impl.doubleGift;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.PioneerGiftSelectReq;

/**
 * 双享豪礼活动
 */
public class DoubleGiftActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 请求活动信息
	 * 
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.DOUBLE_GIFT_INFO_REQ_VALUE)
	public void onGetActivityInfo(HawkProtocol hawkProtocol, String playerId) {
		DoubleGiftActivity activity = this.getActivity(ActivityType.DOUBLE_GIFT_ACTIVITY);
		int result = activity.syncDoubleGiftInfo(playerId);
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
	@ProtocolHandler(code = HP.code.DOUBLE_GIFT_FREE_REQ_VALUE)
	public void onTakeFreeReward(HawkProtocol hawkProtocol, String playerId) {
		DoubleGiftActivity activity = this.getActivity(ActivityType.DOUBLE_GIFT_ACTIVITY);
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
	@ProtocolHandler(code = HP.code.DOUBLE_GIFT_SELECT_REQ_VALUE)
	public void onSelectPioneerGift(HawkProtocol hawkProtocol, String playerId) {
		PioneerGiftSelectReq req = hawkProtocol.parseProtocol(PioneerGiftSelectReq.getDefaultInstance());
		DoubleGiftActivity activity = this.getActivity(ActivityType.DOUBLE_GIFT_ACTIVITY);
		int result = activity.selectDoubleGiftReward(playerId, req.getGiftId(), req.getType());
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			this.sendErrorAndBreak(playerId, hawkProtocol.getType(), result);
		}
	}
	
}
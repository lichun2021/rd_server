package com.hawk.activity.type.impl.giftzero;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.GiftZeroBuyReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

/**
 * 0元礼包活动
 * 
 * @author lating
 *
 */
public class GiftZeroActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 请求礼包信息
	 * 
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.GIFT_ZERO_INFO_REQ_VALUE)
	public void onGetGiftZeroInfo(HawkProtocol hawkProtocol, String playerId) {
		GiftZeroActivity activity = this.getActivity(ActivityType.GIFT_ZERO_ACTIVITY);
		int result = activity.syncGiftZeroInfo(playerId);
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			this.sendErrorAndBreak(playerId, hawkProtocol.getType(), result);
		}
	}
	
	/**
	 * 礼包购买请求
	 * 
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.GIFT_ZERO_BUY_REQ_VALUE)
	public void onBuyGiftReq(HawkProtocol hawkProtocol, String playerId) {
		GiftZeroBuyReq req = hawkProtocol.parseProtocol(GiftZeroBuyReq.getDefaultInstance());
		GiftZeroActivity activity = this.getActivity(ActivityType.GIFT_ZERO_ACTIVITY);
		int result = activity.buyGift(playerId, req.getGiftId());
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			this.sendErrorAndBreak(playerId, hawkProtocol.getType(), result);
		}
	}
	
}
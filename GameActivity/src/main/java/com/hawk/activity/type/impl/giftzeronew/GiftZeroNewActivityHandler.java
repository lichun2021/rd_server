package com.hawk.activity.type.impl.giftzeronew;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.GiftZeroNewBuyReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

/**
 * 新0元礼包活动
 * 
 * @author lating
 *
 */
public class GiftZeroNewActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 请求礼包信息
	 * 
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.GIFT_ZERO_NEW_INFO_REQ_VALUE)
	public void onGetGiftZeroInfo(HawkProtocol hawkProtocol, String playerId) {
		GiftZeroNewActivity activity = this.getActivity(ActivityType.GIFT_ZERO_NEW_ACTIVITY);
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
	@ProtocolHandler(code = HP.code.GIFT_ZERO_NEW_BUY_REQ_VALUE)
	public void onBuyGiftReq(HawkProtocol hawkProtocol, String playerId) {
		GiftZeroNewBuyReq req = hawkProtocol.parseProtocol(GiftZeroNewBuyReq.getDefaultInstance());
		GiftZeroNewActivity activity = this.getActivity(ActivityType.GIFT_ZERO_NEW_ACTIVITY);
		int result = activity.buyGift(playerId, req.getGiftId());
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			this.sendErrorAndBreak(playerId, hawkProtocol.getType(), result);
		}
	}
	
	/**
	 * 免费奖励领取
	 * 
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.GIFT_ZERO_NEW_FREEREWARD_REQ_VALUE)
	public void onFreeRewardReq(HawkProtocol hawkProtocol, String playerId) {
		GiftZeroNewActivity activity = this.getActivity(ActivityType.GIFT_ZERO_NEW_ACTIVITY);
		int result = activity.freeTakeReward(playerId);
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			this.sendErrorAndBreak(playerId, hawkProtocol.getType(), result);
		}
	}
	
}
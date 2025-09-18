package com.hawk.activity.type.impl.dailyrechargenew;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.RechargeBuyGiftCustomReq;
import com.hawk.game.protocol.Activity.RechargeBuyGiftReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

/**
 * 今日累充活动（新版）
 * 
 * @author lating
 *
 */
public class DailyRechargeNewActivityHandler extends ActivityProtocolHandler {

	/**
	 * 特价礼包信息请求
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.RECHARGE_BUY_GIFG_INFO_C_VALUE)
	public boolean onGiftInfoReq(HawkProtocol protocol, String playerId) {
		DailyRechargeNewActivity activity = getActivity(ActivityType.DAILY_RECHARGE_NEW_ACTIVITY);
		int result = activity.syncGiftItemInfo(playerId);
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
			return false;
		}
		
		return true;
	}
	
	/**
	 * 特价礼包奖励选择
	 * 
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.RECHARGE_BUY_CUSTOM_C_VALUE)
	public boolean onGiftRewardSelect(HawkProtocol protocol, String playerId) {
		RechargeBuyGiftCustomReq req = protocol.parseProtocol(RechargeBuyGiftCustomReq.getDefaultInstance());
		DailyRechargeNewActivity activity = getActivity(ActivityType.DAILY_RECHARGE_NEW_ACTIVITY);
		int result = activity.selectGiftReward(playerId, req.getGiftId(), req.getRewardIdList());
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
			return false;
		}
		
		return true;
	}
	
	/**
	 * 购买特价礼包
	 * 
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.RECHARGE_BUY_GIFT_BUY_C_VALUE)
	public boolean onGiftBuyReq(HawkProtocol protocol, String playerId) {
		RechargeBuyGiftReq req = protocol.parseProtocol(RechargeBuyGiftReq.getDefaultInstance());
		DailyRechargeNewActivity activity = getActivity(ActivityType.DAILY_RECHARGE_NEW_ACTIVITY);
		int result = activity.buyGift(playerId, req.getGiftId(), protocol.getType());
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
			return false;
		}
		
		return true;
	}

}

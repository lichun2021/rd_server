package com.hawk.activity.type.impl.dailyrecharge;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.DailyRechargeAccBuyReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

/**
 * 今日累充活动
 * 
 * @author lating
 *
 */
public class DailyRechargeActivityHandler extends ActivityProtocolHandler {

	/**
	 * 今日累充活动已购买宝箱信息请求
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.DAILY_RECHARGE_BOUGHT_REQ_VALUE)
	public boolean onBoxBoughtInfoReq(HawkProtocol protocol, String playerId) {
		DailyRechargeActivity activity = getActivity(ActivityType.DAILY_RECHARGE_ACC_ACTIVITY);
		int result = activity.syncBoughtBoxInfo(playerId);
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
			return false;
		}
		
		return true;
	}
	
	/**
	 * 宝箱购买请求
	 * 
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.DAILY_RECHARGE_BUY_REQ_VALUE)
	public boolean onBoxBuyReq(HawkProtocol protocol, String playerId) {
		DailyRechargeAccBuyReq req = protocol.parseProtocol(DailyRechargeAccBuyReq.getDefaultInstance());
		DailyRechargeActivity activity = getActivity(ActivityType.DAILY_RECHARGE_ACC_ACTIVITY);
		int result = activity.buyRewardBox(playerId, req.getBoxId());
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
			return false;
		}
		
		return true;
	}

}

package com.hawk.activity.type.impl.lotteryDraw;


import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.DoLotteryDrawReq;
import com.hawk.game.protocol.HP;

/**
 * 十连抽活动网络消息接收句柄
 * @author Jesse
 *
 */
public class LotteryDrawActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 获取活动界面信息
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GET_LOTTERY_DRAW_PAGE_INFO_C_VALUE)
	public boolean onGetPageInfo(HawkProtocol protocol, String playerId) {
		LotteryDrawActivity activity = getActivity(ActivityType.LOTTERY_DRAW_ACTIVITY);
		activity.onGetPageInfo(playerId);
		return true;
	}
	
	/**
	 * 抽奖
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.DO_LOTTERY_DRAW_C_VALUE)
	public boolean doLotteryDraw(HawkProtocol protocol, String playerId) {
		LotteryDrawActivity activity = getActivity(ActivityType.LOTTERY_DRAW_ACTIVITY);
		DoLotteryDrawReq req = protocol.parseProtocol(DoLotteryDrawReq.getDefaultInstance());
		activity.onDoLotteryDraw(playerId, req.getTenTimes());
		return true;
	}

}

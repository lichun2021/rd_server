package com.hawk.activity.type.impl.warFlagTwo;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.WarFlagTwoMsg;
import com.hawk.game.protocol.Activity.WarFlagTwoTips;
import com.hawk.game.protocol.HP;

/**
 * 
 * @author golden
 *
 */
public class WarFlagTwoHandler extends ActivityProtocolHandler {

	/***
	 * 前端兑换提醒勾勾
	 */
	@ProtocolHandler(code = HP.code.WAR_FLAG_TWO_TIPS_C_VALUE)
	public void domeExchangePlayerTips(HawkProtocol protocol, String playerId) {
		WarFlagTwoActivity activity = getActivity(ActivityType.WAR_FLAG_TWO_ACTIVITY);
		WarFlagTwoTips req = protocol.parseProtocol(WarFlagTwoTips.getDefaultInstance());
		int id = req.getId();
		int tip = req.getTip();
		Result<?> result = activity.reqActivityTips(playerId, id, tip);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}

	/***
	 * 兑换接口
	 */
	@ProtocolHandler(code = HP.code.WAR_FLAG_TWO_EXCHANGE_VALUE)
	public void domeExchangeExchange(HawkProtocol protocol, String playerId) {
		WarFlagTwoActivity activity = getActivity(ActivityType.WAR_FLAG_TWO_ACTIVITY);
		WarFlagTwoMsg msg = protocol.parseProtocol(WarFlagTwoMsg.getDefaultInstance());
		int count = msg.getNum();
		if (count <= 0) {
			throw new RuntimeException("zeor input");
		}
		Result<Integer> result = activity.brokenExchange(playerId, msg.getExchangeId(), count);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
}
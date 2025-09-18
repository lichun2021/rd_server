package com.hawk.activity.type.impl.dailysign;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.PBDailySignResignReq;
import com.hawk.game.protocol.HP;

public class DailySignHandler extends ActivityProtocolHandler {

	// 签到 
	@ProtocolHandler(code = HP.code.DAILY_SIGN_SIGN_REQ_C_VALUE)
	public void onPlayerSign(HawkProtocol protocol, String playerId) {
		DailySignActivity activity = getActivity(ActivityType.DAILY_SIGN_ACTIVITY);
		if (null == activity) {
			LoggerFactory.getLogger("Server").error("dailysign_log DailySign activity not open");
			return;
		}
		if (!activity.isOpening(playerId)) {
			LoggerFactory.getLogger("Server").error("dailysign_log DailySign activity not open for player:{}", playerId);
			return;
		}
		activity.onPlayerSign(playerId);
	}

	// 补签
	@ProtocolHandler(code = HP.code.DAILY_SIGN_RESIGN_REQ_C_VALUE)
	public void onPlayerResign(HawkProtocol protocol, String playerId) {
		DailySignActivity activity = getActivity(ActivityType.DAILY_SIGN_ACTIVITY);
		PBDailySignResignReq req = protocol.parseProtocol(PBDailySignResignReq.getDefaultInstance());
		if (null == activity) {
			LoggerFactory.getLogger("Server").error("dailysign_log DailySign activity not open");
			return;
		}
		if (!activity.isOpening(playerId)) {
			LoggerFactory.getLogger("Server").error("dailysign_log DailySign activity not open for player:{}", playerId);
			return;
		}
		activity.onPlayerResign(playerId, req);
	}

}

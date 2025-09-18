package com.hawk.activity.type.impl.medalAction;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.MedalLotteryReq;
import com.hawk.game.protocol.Activity.MedalLotteryResponse;
import com.hawk.game.protocol.HP;

public class MedalActionHandler extends ActivityProtocolHandler {

	@ProtocolHandler(code = HP.code.MEDAL_ACTION_LOTTERY_REQ_VALUE)
	public void medalTreasureLottery(HawkProtocol protocol, String playerId){
		MedalActionActivity activity = getActivity(ActivityType.MEDAL_ACTION_ACTIVITY);
		MedalLotteryReq req = protocol.parseProtocol(MedalLotteryReq.getDefaultInstance());
		boolean isTenTimes = req.getIsTenTimes();
		Result<?> result = activity.lottery(playerId, isTenTimes);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
		MedalLotteryResponse.Builder builder = (MedalLotteryResponse.Builder) result.getRetObj();
		sendProtocol(playerId, HawkProtocol.valueOf(HP.code.MEDAL_ACTION_LOTTERY_RESP_VALUE, builder));
	}
}

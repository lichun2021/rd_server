package com.hawk.activity.type.impl.brokenexchangeThree;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.helper.HawkAssert;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.BrokenExchangeReq;
import com.hawk.game.protocol.HP;

public class BrokenExchangeThreeHandler extends ActivityProtocolHandler {
	@ProtocolHandler(code = HP.code.BROKEN_EXCHANGE_THREE_REQ_VALUE)
	public void brokenExchange(HawkProtocol protocol, String playerId) {
		BrokenExchangeThreeActivity activity = getActivity(ActivityType.BROKEN_EXCHANGE_THREE);
		BrokenExchangeReq brokenExchangeReq = protocol.parseProtocol(BrokenExchangeReq.getDefaultInstance());
		int count = brokenExchangeReq.getNum();
		HawkAssert.checkPositive(count);
		if (count <= 0) {
			throw new RuntimeException("zeor input");
		}
		
		Result<Integer> result = activity.brokenExchange(playerId, brokenExchangeReq.getExchangeId(),
				count);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}

}

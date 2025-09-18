package com.hawk.activity.type.impl.timeLimitDrop;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;

public class TimeLimitDropHandler extends ActivityProtocolHandler {
	
	@ProtocolHandler(code = HP.code.TIME_LIMIT_DROP_RANK_REQ_VALUE)
	public void onRankReq(HawkProtocol protocol, String playerId) {
		TimeLimitDropActivity activity = getActivity(ActivityType.TIME_LIMIT_DROP);
		if (activity.isAllowOprate(playerId)) {
			activity.onRankInfoReq(playerId);
		}
	}
}

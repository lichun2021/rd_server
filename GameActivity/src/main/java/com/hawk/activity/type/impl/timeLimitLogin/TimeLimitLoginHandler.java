package com.hawk.activity.type.impl.timeLimitLogin;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.LimitLoginRewardReq;
import com.hawk.game.protocol.HP;

public class TimeLimitLoginHandler extends ActivityProtocolHandler {
	//限时登录领取奖励
	@ProtocolHandler(code = HP.code.TIME_LIMIT_LOGIN_REWARD_REQ_VALUE)
	public void onRankReq(HawkProtocol protocol, String playerId) {
		TimeLimitLoginActivity activity = getActivity(ActivityType.TIME_LIMIT_LOGIN_ACTIVITY);
		LimitLoginRewardReq req = protocol.parseProtocol(LimitLoginRewardReq.getDefaultInstance());
		if (activity.isAllowOprate(playerId)) {
			Result<?> result = activity.receiveReward(playerId, req.getId());
			if (result.isFail()) {
				sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
				return;
			}
		}
	}
	//限时登录数据同步
	@ProtocolHandler(code = HP.code.TIME_LIMIT_LOGIN_INFO_REQ_VALUE)
	public void timeLimitLoginInfo(HawkProtocol protocol, String playerId) {
		TimeLimitLoginActivity activity = getActivity(ActivityType.TIME_LIMIT_LOGIN_ACTIVITY);
		activity.syncActivityDataInfo(playerId);
	}
}

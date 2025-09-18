package com.hawk.activity.type.impl.buildlevel;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.TakeBuildActivityRewardReq;
import com.hawk.game.protocol.HP;

/**
 * 建筑等级活动网络消息接收句柄
 * @author PhilChen
 *
 */
public class BuildLevelActivityHandler extends ActivityProtocolHandler {

	/**
	 * 领取（基地等级）活动项奖励
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.TAKE_BUILD_LEVEL_REWARD_C_VALUE)
	public boolean onActivityReward(HawkProtocol protocol, String playerId) {
		TakeBuildActivityRewardReq req = protocol.parseProtocol(TakeBuildActivityRewardReq.getDefaultInstance());
		BuildLevelActivity activity = getActivity(ActivityType.BUILD_LEVEL_ACTIVITY);
		int itemId = req.getItemId();
		Result<?> result = activity.takeRewards(playerId, itemId);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return false;
		}
		return true;
	}

}

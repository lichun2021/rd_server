package com.hawk.activity.type.impl.fireworks;

import java.util.Optional;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.SendFireWorksReq;
import com.hawk.game.protocol.HP;

public class FireWorksHandler extends ActivityProtocolHandler {

	//烟花盛典免费领取奖励
	@ProtocolHandler(code = HP.code.FIREWORKS_RECEIVE_REWARD_REQ_VALUE)
	public void onActivityRefresh(HawkProtocol protocol, String playerId) {
		Optional<ActivityBase> activityOp = ActivityManager.getInstance().getActivity(Activity.ActivityType.FIRE_WORKS_VALUE);
		if (activityOp.isPresent()) {
			FireWorksActivity activity = (FireWorksActivity) activityOp.get();
			if (activity.isOpening(playerId)) {
				Result<?> result = activity.getFreeFireReward(playerId, protocol.getType());
				if (result.isFail()) {
					sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
				} else {
					responseSuccess(playerId, protocol.getType());
				}				
			}
		}
	}

	@ProtocolHandler(code = HP.code.FIREWORKS_LIGHT_REQ_VALUE)
	public void onActivityActiveBuff(HawkProtocol protocol, String playerId) {
		Optional<ActivityBase> activityOp = ActivityManager.getInstance().getActivity(Activity.ActivityType.FIRE_WORKS_VALUE);
		if (activityOp.isPresent()) {
			FireWorksActivity activity = (FireWorksActivity) activityOp.get();
			if (activity.isOpening(playerId)) {
				SendFireWorksReq req = protocol.parseProtocol(SendFireWorksReq.getDefaultInstance());
				Result<?> result = activity.lightFireWorks(playerId, req.getNumber(), protocol.getType());
				if (result.isFail()) {
					sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
				} else {
					responseSuccess(playerId, protocol.getType());
				}
			}

		}
	}

	@ProtocolHandler(code = HP.code.FIREWORKS_INFO_REQ_VALUE)
	public void onActivityBuy(HawkProtocol protocol, String playerId) {
		Optional<ActivityBase> activityOp = ActivityManager.getInstance().getActivity(Activity.ActivityType.BLACK_TECH_VALUE);
		if (activityOp.isPresent()) {
			FireWorksActivity activity = (FireWorksActivity) activityOp.get();
			if (activity.isOpening(playerId)) {
				activity.syncActivityDataInfo(playerId);		
			}  else {
				responseSuccess(playerId, protocol.getType());
			}
		}
	}
}

package com.hawk.game.script;

import java.util.Map;
import java.util.Optional;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.impl.order.activityOrderTwo.OrderTwoActivity;
import com.hawk.activity.type.impl.order.activityOrderTwo.entity.OrderTwoEntity;
import com.hawk.game.protocol.Activity;

public class AddOrderActivityExpHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo arg1) {
		
		String playerId = params.get("playerId");
		String exp = params.get("exp");
		
		Optional<ActivityBase> activityOp = ActivityManager.getInstance().getActivity(Activity.ActivityType.ORDER_TWO_VALUE);
		
		OrderTwoActivity activity = (OrderTwoActivity) activityOp.get();
		Optional<OrderTwoEntity> opEntity = activity.getPlayerDataEntity(playerId);
		if (opEntity.isPresent()) {
			OrderTwoEntity entity = opEntity.get();
			activity.addExp(entity, Integer.parseInt(exp), 0, 0);
			activity.syncActivityDataInfo(playerId);
		}
		return HawkScript.successResponse(playerId+":"+exp);
	}

}

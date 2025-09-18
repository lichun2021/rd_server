package com.hawk.activity.type.impl.skinPlan;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

public class SkinPlanHandler extends ActivityProtocolHandler{

	/**
	 * 皮肤计划信息
	 */
	@ProtocolHandler(code=HP.code.SKIN_PLAN_INFO_REQ_VALUE)
	public void goldTowerInfo(HawkProtocol protocol,String playerId){
		SkinPlanActivity activity = getActivity(ActivityType.SKIN_PLAN_ACTIVITY);
		if (activity == null) {
			sendErrorAndBreak(playerId, protocol.getType(), Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			return;
		}
		activity.sycGoldTowerInfo(playerId);
	}
	/**
	 * 皮肤计划扔骰子
	 */
	@ProtocolHandler(code=HP.code.SKIN_PLAN_ROLL_DICE_REQ_VALUE)
	public void goleTowerRollDice(HawkProtocol protocol, String playerId){
		SkinPlanActivity activity = getActivity(ActivityType.SKIN_PLAN_ACTIVITY);
		if (activity == null) {
			sendErrorAndBreak(playerId, protocol.getType(), Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			return;
		}
		Result<?> result = activity.goldTowerRollDice(playerId);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}else{
			responseSuccess(playerId, protocol.getType());
		}
		
		
	}
}

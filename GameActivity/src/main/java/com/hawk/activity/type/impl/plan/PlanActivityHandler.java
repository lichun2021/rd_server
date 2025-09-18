package com.hawk.activity.type.impl.plan;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.HPPlanLotteryReq;
import com.hawk.game.protocol.HP;

public class PlanActivityHandler extends ActivityProtocolHandler {

	/***
	 * 源计划抽奖
	 * 
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.PLAN_LOTTERY_REQ_C_VALUE)
	public void onPlanActivityLottery(HawkProtocol protocol, String playerId) {
		PlanActivity activity = getActivity(ActivityType.PLAN_ACTIVITY);
		if (null != activity) {
			HPPlanLotteryReq req = protocol.parseProtocol(HPPlanLotteryReq.getDefaultInstance());
			int type = req.getLotteryType();
			Result<?> result = activity.planActivityLottery(playerId, type);
			if (result.isFail()) {
				sendError(playerId, protocol.getType(), result.getStatus());
			}
		}

	}

	/***
	 * 源计划排行榜数据清秀
	 * 
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.PLAN_RANK_REQ_C_VALUE)
	public void onPlanActivityRank(HawkProtocol protocol, String playerId) {
		PlanActivity activity = getActivity(ActivityType.PLAN_ACTIVITY);
		if(null != activity){
			activity.sendPlanRankToPlayer(playerId);
		}
	}
	
	/***
	 * 源计划基本信息请求
	 * 
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.PLAN_INFO_REQ_C_VALUE)
	public void onPlanActivityInfoReq(HawkProtocol protocol, String playerId) {
		PlanActivity activity = getActivity(ActivityType.PLAN_ACTIVITY);
		if(null != activity){
			activity.syncActivityDataInfo(playerId);
		}
	}
}

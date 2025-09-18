package com.hawk.activity.type.impl.order.activityNewOrder;


import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.AchieveNewOrderLevelRewardReq;
import com.hawk.game.protocol.Activity.BuyNewOrderExpReq;
import com.hawk.game.protocol.HP;

/**
 * 登录基金活动网络消息接收句柄
 * 
 * @author Jesse
 *
 */
public class NewOrderActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 获取活动界面信息
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.NEW_ORDER_GET_PAGE_INFO_C_VALUE)
	public boolean onGetPageInfo(HawkProtocol protocol, String playerId) {
		NewOrderActivity activity = getActivity(ActivityType.NEW_ORDER_ACTIVITY);
		activity.syncActivityDataInfo(playerId);
		return true;
	}
	
	/**
	 * 购买经验礼包
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.NEW_ORDER_BUY_EXP_C_VALUE)
	public boolean onBuyExp(HawkProtocol protocol, String playerId) {
		BuyNewOrderExpReq req =  protocol.parseProtocol(BuyNewOrderExpReq.getDefaultInstance());
		NewOrderActivity activity = getActivity(ActivityType.NEW_ORDER_ACTIVITY);
		Result<?> result = activity.buyOrderExp(playerId, req.getExpId());
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return false;
		}
		responseSuccess(playerId, protocol.getType());
		return true;
	}
	
	
	@ProtocolHandler(code = HP.code.NEW_ORDER_GET_LEVEL_REWARD_C_VALUE)
	public boolean ongetOrderLevelReward(HawkProtocol protocol, String playerId) {
		AchieveNewOrderLevelRewardReq req =  protocol.parseProtocol(AchieveNewOrderLevelRewardReq.getDefaultInstance());
		NewOrderActivity activity = getActivity(ActivityType.NEW_ORDER_ACTIVITY);
		//activity.getLevelReward(playerId, req.getLevel());
		activity.getLevelRewardOneKey(playerId);
		return true;
	}
	

}

package com.hawk.activity.type.impl.order;


import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.BuyOrderExpReq;
import com.hawk.game.protocol.Activity.GetHistoryOrderInfoReq;
import com.hawk.game.protocol.HP;

/**
 * 登录基金活动网络消息接收句柄
 * 
 * @author Jesse
 *
 */
public class OrderActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 获取活动界面信息
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.ORDER_GET_PAGE_INFO_C_VALUE)
	public boolean onGetPageInfo(HawkProtocol protocol, String playerId) {
		OrderActivity activity = getActivity(ActivityType.ORDER_ACTIVITY);
		activity.syncActivityDataInfo(playerId);
		return true;
	}
	
	/**
	 * 购买经验礼包
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.ORDER_BUY_EXP_C_VALUE)
	public boolean onBuyExp(HawkProtocol protocol, String playerId) {
		BuyOrderExpReq req =  protocol.parseProtocol(BuyOrderExpReq.getDefaultInstance());
		OrderActivity activity = getActivity(ActivityType.ORDER_ACTIVITY);
		Result<?> result = activity.buyOrderExp(playerId, req.getExpId());
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return false;
		}
		responseSuccess(playerId, protocol.getType());
		return true;
	}
	
	/**
	 * 获取历史周任务信息
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.ORDER_GET_HISTORY_INFO_C_VALUE)
	public boolean onGetHisttoryInfo(HawkProtocol protocol, String playerId) {
		GetHistoryOrderInfoReq req = protocol.parseProtocol(GetHistoryOrderInfoReq.getDefaultInstance());
		OrderActivity activity = getActivity(ActivityType.ORDER_ACTIVITY);
		activity.getGetHistoryInfo(playerId, req.getCycleId());
		return true;
	}
	

}

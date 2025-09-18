package com.hawk.activity.type.impl.order.activityOrderTwo;


import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.BuyOrderLvlReq;
import com.hawk.game.protocol.Activity.GetHistoryOrderInfoReq;
import com.hawk.game.protocol.Activity.OrderRewardAchieveReq;
import com.hawk.game.protocol.Activity.OrderShopBuyReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

/**
 * 新版战令消息接收句柄
 * 
 * @author Jesse
 *
 */
public class OrderTwoActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 获取活动界面信息
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.ORDER_TWO_GET_PAGE_INFO_C_VALUE)
	public boolean onGetPageInfo(HawkProtocol protocol, String playerId) {
		OrderTwoActivity activity = getActivity(ActivityType.ORDER_TWO_ACTIVITY);
		activity.syncActivityDataInfo(playerId);
		return true;
	}
	
	
	/**
	 * 获取历史周任务信息
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.ORDER_TWO_GET_HISTORY_INFO_C_VALUE)
	public boolean onGetHisttoryInfo(HawkProtocol protocol, String playerId) {
		GetHistoryOrderInfoReq req = protocol.parseProtocol(GetHistoryOrderInfoReq.getDefaultInstance());
		OrderTwoActivity activity = getActivity(ActivityType.ORDER_TWO_ACTIVITY);
		activity.getGetHistoryInfo(playerId, req.getCycleId());
		return true;
	}
	
	/**
	 * 购买战令等级
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.ORDER_TWO_BUY_LEVEL_C_VALUE)
	public boolean onBuyOrderLvl(HawkProtocol protocol, String playerId) {
		BuyOrderLvlReq req = protocol.parseProtocol(BuyOrderLvlReq.getDefaultInstance());
		OrderTwoActivity activity = getActivity(ActivityType.ORDER_TWO_ACTIVITY);
		Result<?> result = activity.buyOrderLevel(playerId, req.getCurrLvl());
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return false;
		}
		responseSuccess(playerId, protocol.getType());
		return true;
	}
	
	/**
	 * 购买战令等级
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.ORDER_TWO_SHOP_BUY_C_VALUE)
	public boolean onBuyOrderShop(HawkProtocol protocol, String playerId) {
		OrderShopBuyReq req = protocol.parseProtocol(OrderShopBuyReq.getDefaultInstance());
		OrderTwoActivity activity = getActivity(ActivityType.ORDER_TWO_ACTIVITY);
		Result<?> result = activity.orderShopBuy(playerId, req.getItemInfo());
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return false;
		}
		responseSuccess(playerId, protocol.getType());
		return true;
	}
	
	
	/**
	 * 购买战令等级
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.ORDER_TWO_REWARD_ACHIEVE_C_VALUE)
	public boolean onRewardAchieve(HawkProtocol protocol, String playerId) {
		OrderRewardAchieveReq req = protocol.parseProtocol(OrderRewardAchieveReq.getDefaultInstance());
		OrderTwoActivity activity = getActivity(ActivityType.ORDER_TWO_ACTIVITY);
		int type = req.getRewardType();
		Result<?> result = Result.fail(Status.SysError.DATA_ERROR_VALUE);
		if(type == 1){
			result = activity.orderRewardNormal(playerId,req.getRewardLevelList());
		}else if(type == 2){
			result = activity.orderRewardAdvance(playerId,req.getRewardLevelList());
		}
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return false;
		}
		responseSuccess(playerId, protocol.getType());
		return true;
	}
	
	
	@ProtocolHandler(code = HP.code2.ORDER_TWO_REWARD_ACHIEVE_ALL_C_VALUE)
	public boolean onRewardAchieveAll(HawkProtocol protocol, String playerId) {
		OrderTwoActivity activity = getActivity(ActivityType.ORDER_TWO_ACTIVITY);
		Result<?> result = activity.orderRewardAll(playerId);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return false;
		}
		responseSuccess(playerId, protocol.getType());
		return true;
	}
	

}

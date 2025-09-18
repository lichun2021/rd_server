package com.hawk.activity.type.impl.order.activityEquipOrder;


import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.BuyOrderEquipLvlReq;
import com.hawk.game.protocol.Activity.GetHistoryOrderEquipInfoReq;
import com.hawk.game.protocol.HP;

/**
 * 新版战令消息接收句柄
 * 
 * @author Jesse
 *
 */
public class OrderEquipActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 获取活动界面信息
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.ORDER_EQUIP_GET_PAGE_INFO_C_VALUE)
	public boolean onGetPageInfo(HawkProtocol protocol, String playerId) {
		OrderEquipActivity activity = getActivity(ActivityType.ORDER_EQUIP_ACTIVITY);
		activity.syncActivityDataInfo(playerId);
		return true;
	}
	
	
	/**
	 * 获取历史周任务信息
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.ORDER_EQUIP_GET_HISTORY_INFO_C_VALUE)
	public boolean onGetHisttoryInfo(HawkProtocol protocol, String playerId) {
		GetHistoryOrderEquipInfoReq req = protocol.parseProtocol(GetHistoryOrderEquipInfoReq.getDefaultInstance());
		OrderEquipActivity activity = getActivity(ActivityType.ORDER_EQUIP_ACTIVITY);
		activity.getGetHistoryInfo(playerId, req.getCycleId());
		return true;
	}
	
	/**
	 * 购买战令等级
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.ORDER_EQUIP_BUY_LEVEL_C_VALUE)
	public boolean onBuyOrderLvl(HawkProtocol protocol, String playerId) {
		BuyOrderEquipLvlReq req = protocol.parseProtocol(BuyOrderEquipLvlReq.getDefaultInstance());
		OrderEquipActivity activity = getActivity(ActivityType.ORDER_EQUIP_ACTIVITY);
		Result<?> result = activity.buyAuthLvl(playerId, req.getCurrLvl());
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return false;
		}
		responseSuccess(playerId, protocol.getType());
		return true;
	}
	
	
	
	

}

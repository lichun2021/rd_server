package com.hawk.activity.type.impl.flightplan;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.CellMoveReq;
import com.hawk.game.protocol.Activity.GoodsExchangeReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

/**
 * 威龙庆典-飞行计划
 * 
 * @author lating
 *
 */
public class FlightPlanActivityHandler extends ActivityProtocolHandler {

	/**
	 * 摇骰子
	 * 
	 * @param protocol
	 * @param playerId
	 * 
	 * @return
	 */
	@ProtocolHandler(code = HP.code.FLIGHT_PLAN_CELL_MOVE_REQ_VALUE)
	public boolean onFlight(HawkProtocol protocol, String playerId) {
		FlightPlanActivity activity = getActivity(ActivityType.FLIGHT_PLAN_ACTIVITY);
		CellMoveReq req = protocol.parseProtocol(CellMoveReq.getDefaultInstance());
		int code = activity.onRollDice(playerId, req.getType(), protocol.getType());
		if (code != Status.SysError.SUCCESS_OK_VALUE) {
			sendErrorAndBreak(playerId, protocol.getType(), code);
			return false;
		}
		
		responseSuccess(playerId, protocol.getType());
		return true;
	}
	
	/**
	 * 商品兑换成请求
	 * 
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.FLIGHT_PLAN_EXCHANGE_ERQ_VALUE)
	public boolean onExchange(HawkProtocol protocol, String playerId) {
		GoodsExchangeReq req = protocol.parseProtocol(GoodsExchangeReq.getDefaultInstance());
		FlightPlanActivity activity = getActivity(ActivityType.FLIGHT_PLAN_ACTIVITY);
		int code = activity.onExchange(playerId, req.getGoodsId(), req.getCount(), protocol.getType());
		if (code != Status.SysError.SUCCESS_OK_VALUE) {
			sendErrorAndBreak(playerId, protocol.getType(), code);
			return false;
		}
		
		responseSuccess(playerId, protocol.getType());
		return true;
	}
	
	/**
	 * 请求活动页面信息
	 * 
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.FLIGHT_PLAN_PAGE_INFO_REQ_VALUE)
	public boolean onShare(HawkProtocol protocol, String playerId) {
		FlightPlanActivity activity = getActivity(ActivityType.FLIGHT_PLAN_ACTIVITY);
		if (!activity.isOpening(playerId)) {
			sendErrorAndBreak(playerId, protocol.getType(), Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			return false;
		}
		
		activity.syncCellInfo(playerId);
		responseSuccess(playerId, protocol.getType());
		return true;
	}
}

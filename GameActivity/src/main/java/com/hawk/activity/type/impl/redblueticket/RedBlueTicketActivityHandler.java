package com.hawk.activity.type.impl.redblueticket;


import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.RedBlueTicketOpenReq;
import com.hawk.game.protocol.HP;

/**
 * 红蓝对决翻牌活动
 * 
 * @author lating
 *
 */
public class RedBlueTicketActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 翻牌
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.RED_BLUE_TICKET_OPEN_VALUE)
	public boolean openTicket(HawkProtocol protocol, String playerId) {
		RedBlueTicketOpenReq req = protocol.parseProtocol(RedBlueTicketOpenReq.getDefaultInstance());
		RedBlueTicketActivity activity = getActivity(ActivityType.REDBLUE_TICKET_ACTIVITY);
		Result<?> result = activity.openTicket(playerId, req.getPoolId(), req.getTicketId());
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return false;
		}
		responseSuccess(playerId, protocol.getType());
		return true;
	}
	
	/**
	 * 牌面刷新重置
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.RED_BLUE_TICKET_REFRESH_VALUE)
	public boolean refreshTicket(HawkProtocol protocol, String playerId) {
		RedBlueTicketActivity activity = getActivity(ActivityType.REDBLUE_TICKET_ACTIVITY);
		Result<?> result = activity.refreshTick(playerId);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return false;
		}
		responseSuccess(playerId, protocol.getType());
		return true;
	}
	
	/**
	 * 开启翻牌
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.RED_BLUE_START_OPEN_VALUE)
	public boolean openStart(HawkProtocol protocol, String playerId) {
		RedBlueTicketActivity activity = getActivity(ActivityType.REDBLUE_TICKET_ACTIVITY);
		Result<?> result = activity.openStart(playerId);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return false;
		}
		responseSuccess(playerId, protocol.getType());
		return true;
	}
	
}

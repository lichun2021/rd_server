package com.hawk.activity.type.impl.joybuy;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.JoyBuyOperationReq;
import com.hawk.game.protocol.HP;

/**
 * 欢乐购活动
 * 
 * 1、请求兑换列表
 * 2、兑换
 * 3、刷新
 * @author luke
 */
public class JoyBuyActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 兑换列表
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.JOY_BUY_EXCHANGE_LIST_REQ_VALUE)
	public void onExchangeList(HawkProtocol hawkProtocol, String playerId) {
		JoyBuyActivity activity = this.getActivity(ActivityType.JOY_BUY_ACTIVITY);
		activity.exchangeList(playerId);
	}
	
	/**
	 * 兑换
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.JOY_BUY_EXCHANGE_OPERATION_REQ_VALUE)
	public void onExchangeOperation(HawkProtocol hawkProtocol, String playerId) {
		JoyBuyOperationReq req = hawkProtocol.parseProtocol(JoyBuyOperationReq.getDefaultInstance());
		JoyBuyActivity activity = this.getActivity(ActivityType.JOY_BUY_ACTIVITY);
		activity.exchangeOperation(playerId, req.getExchangeId(),req.getExchangeNumber());
	}
	
	/**
	 * 刷新列表
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.JOY_BUY_EXCHANGE_REFRESH_REQ_VALUE)
	public void onExchangeRefresh(HawkProtocol hawkProtocol, String playerId) {
		JoyBuyActivity activity = this.getActivity(ActivityType.JOY_BUY_ACTIVITY);
		activity.exchangeRefresh(playerId);
	}
	
}
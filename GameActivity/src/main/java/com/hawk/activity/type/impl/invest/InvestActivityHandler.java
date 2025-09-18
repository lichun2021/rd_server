package com.hawk.activity.type.impl.invest;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.InvestBuyReq;
import com.hawk.game.protocol.Activity.InvestProfitReq;

/**
 * 投资理财活动
 * 
 * 1、请求理财产品实时信息
 * 2、购买理财产品
 * 3、领取理财收益
 * 4、取消投资
 * 
 * @author lating
 *
 */
public class InvestActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 请求理财产品信息
	 * 
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.INVEST_INFO_REQ_VALUE)
	public void onGetInvestInfo(HawkProtocol hawkProtocol, String playerId) {
		InvestActivity activity = this.getActivity(ActivityType.INVEST_ACTIVITY);
		int result = activity.syncInvestProductInfo(playerId);
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			this.sendError(playerId, hawkProtocol.getType(), result);
		}
	}
	
	/**
	 * 投资理财（购买理财产品）请求
	 * 
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.INVEST_BUY_REQ_VALUE)
	public void onInvestBuyReq(HawkProtocol hawkProtocol, String playerId) {
		InvestBuyReq req = hawkProtocol.parseProtocol(InvestBuyReq.getDefaultInstance());
		InvestActivity activity = this.getActivity(ActivityType.INVEST_ACTIVITY);
		int result = activity.productInvest(playerId, req.getProductId(), req.getAmount(), req.getBuyCustomer());
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			this.sendError(playerId, hawkProtocol.getType(), result);
		}
	}
	
	/**
	 * 领取理财收益请求
	 * 
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.INVEST_PROFIT_REQ_VALUE)
	public void onReceiveInvestProfit(HawkProtocol hawkProtocol, String playerId) {
		InvestProfitReq req = hawkProtocol.parseProtocol(InvestProfitReq.getDefaultInstance());
		InvestActivity activity = this.getActivity(ActivityType.INVEST_ACTIVITY);
		int result = activity.receiveInvestProfit(playerId, req.getProductId());
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			this.sendError(playerId, hawkProtocol.getType(), result);
		}
	}
	
	/**
	 * 取消投资
	 * 
	 * @param hawkProtocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.INVEST_CANCEL_REQ_VALUE)
	public void onInvestCancel(HawkProtocol hawkProtocol, String playerId) {
		InvestProfitReq req = hawkProtocol.parseProtocol(InvestProfitReq.getDefaultInstance());
		InvestActivity activity = this.getActivity(ActivityType.INVEST_ACTIVITY);
		int result = activity.investCancel(playerId, req.getProductId());
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			this.sendError(playerId, hawkProtocol.getType(), result);
		}
	}
	
}
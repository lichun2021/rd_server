package com.hawk.activity.type.impl.bestprize;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.BestPrizeBigPoolReq;
import com.hawk.game.protocol.Activity.BestPrizeDrawReq;
import com.hawk.game.protocol.Activity.BestPrizeExchangeReq;
import com.hawk.game.protocol.Activity.BestPrizeShopBuyReq;
import com.hawk.activity.ActivityProtocolHandler;

/**
 * 新春头奖专柜活动
 * @author lating
 *
 */
public class BestPrizeHandler extends ActivityProtocolHandler{
	/**
	 * 请求活动信息
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.BEST_PRIZE_INFO_REQ_VALUE)
	public void pageInfo(HawkProtocol protocol, String playerId){
		BestPrizeActivity activity = getActivity(ActivityType.BEST_PRIZE_361);
		activity.syncActivityInfo(playerId);
	}
	
	/**
	 * 请求单个大奖池的信息
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.BEST_PRIZE_BIGPOOL_REQ_VALUE)
	public void bigPoolInfoReq(HawkProtocol protocol, String playerId){
		BestPrizeActivity activity = getActivity(ActivityType.BEST_PRIZE_361);
		BestPrizeBigPoolReq req = protocol.parseProtocol(BestPrizeBigPoolReq.getDefaultInstance());
		activity.syncBigPoolInfo(playerId, req.getPoolId());
	}
	
	/**
	 * 奖池抽奖
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.BEST_PRIZE_DRAW_REQ_VALUE)
	public void poolDrawReq(HawkProtocol protocol, String playerId){
		BestPrizeActivity activity = getActivity(ActivityType.BEST_PRIZE_361);
		BestPrizeDrawReq req = protocol.parseProtocol(BestPrizeDrawReq.getDefaultInstance());
		int result = activity.onPoolDraw(playerId, req);
		if (result > 0) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
		}
	}
	
	/**
	 * 商店购买
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.BEST_PRIZE_SHOP_BUY_REQ_VALUE)
	public void shopBuyReq(HawkProtocol protocol, String playerId){
		BestPrizeActivity activity = getActivity(ActivityType.BEST_PRIZE_361);
		BestPrizeShopBuyReq req = protocol.parseProtocol(BestPrizeShopBuyReq.getDefaultInstance());
		int result = activity.onShopBuy(playerId, req.getShopId(), req.getCount());
		if (result > 0) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
		}
	}
	
	/**
	 * 积分商店兑换
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code2.BEST_PRIZE_EXCHANGE_REQ_VALUE)
	public void exchangeReq(HawkProtocol protocol, String playerId){
		BestPrizeActivity activity = getActivity(ActivityType.BEST_PRIZE_361);
		BestPrizeExchangeReq req = protocol.parseProtocol(BestPrizeExchangeReq.getDefaultInstance());
		int result = activity.onPointExchange(playerId, req.getExchangeId(), req.getCount());
		if (result > 0) {
			sendErrorAndBreak(playerId, protocol.getType(), result);
		}
	}
	
}

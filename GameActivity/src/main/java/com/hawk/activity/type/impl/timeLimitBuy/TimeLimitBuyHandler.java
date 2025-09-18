package com.hawk.activity.type.impl.timeLimitBuy;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.ActivityTimeLimitBuy.TimeLimitBuyPageInfoReq;
import com.hawk.game.protocol.ActivityTimeLimitBuy.TimeLimitBuyReq;
import com.hawk.game.protocol.HP;

/**
 * 限时抢购
 * @author Golden
 *
 */
public class TimeLimitBuyHandler extends ActivityProtocolHandler{

	/**
	 * 请求界面信息
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.ACTIVITY_TIME_LIMIT_BUY_PAGE_REQ_VALUE)
	public void pageInfo(HawkProtocol protocol, String playerId){
		TimeLimitBuyPageInfoReq req = protocol.parseProtocol(TimeLimitBuyPageInfoReq.getDefaultInstance());
		TimeLimitBuyActivity activity = getActivity(ActivityType.TIME_LIMIT_BUY);
		activity.pushPageInfo(playerId, req.getTurnId());
	}
	
	/**
	 * 抢购
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.ACTIVITY_TIME_LIMIT_BUY_VALUE)
	public void buy(HawkProtocol protocol, String playerId){
		TimeLimitBuyReq req = protocol.parseProtocol(TimeLimitBuyReq.getDefaultInstance());
		TimeLimitBuyActivity activity = getActivity(ActivityType.TIME_LIMIT_BUY);
		activity.buy(playerId, req.getGoodsId());
	}
	
	/**
	 * 关闭提示
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.ACTIVITY_TIME_LIMIT_BUY_CLOSE_REMIND_VALUE)
	public void closeRemind(HawkProtocol protocol, String playerId){
		TimeLimitBuyPageInfoReq req = protocol.parseProtocol(TimeLimitBuyPageInfoReq.getDefaultInstance());
		TimeLimitBuyActivity activity = getActivity(ActivityType.TIME_LIMIT_BUY);
		activity.closeRemind(playerId, req.getTurnId());
	}
}

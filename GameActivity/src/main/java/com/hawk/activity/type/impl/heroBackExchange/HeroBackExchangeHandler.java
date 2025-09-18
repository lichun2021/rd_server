package com.hawk.activity.type.impl.heroBackExchange;

import com.hawk.game.protocol.Activity;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.HeroBackExchangeMsg;
import com.hawk.game.protocol.Activity.HeroBackExchangeTips;
import com.hawk.game.protocol.HP;

public class HeroBackExchangeHandler extends ActivityProtocolHandler {
	
	/***
	 * 穹顶兑换 前端兑换提醒勾勾
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.HERO_BACK_EXCHANGE_TIPS_C_VALUE)
	public void HeroBackExchangePlayerTips(HawkProtocol protocol, String playerId){
		HeroBackExchangeActivity activity = getActivity(ActivityType.HERO_BACK_EXCHANGE);
		HeroBackExchangeTips req = protocol.parseProtocol(HeroBackExchangeTips.getDefaultInstance());
		int id = req.getId();
		int tip = req.getTip();
		Result<?> result = activity.reqActivityTips(playerId, id, tip);
		if(result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
	
	/***
	 * 穹顶兑换 兑换接口
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.HERO_BACK_EXCHANGE_VALUE)
	public void heroBackExchangeExchange(HawkProtocol protocol, String playerId){
		HeroBackExchangeActivity activity = getActivity(ActivityType.HERO_BACK_EXCHANGE);
		HeroBackExchangeMsg msg = protocol.parseProtocol(HeroBackExchangeMsg.getDefaultInstance());
		int count = msg.getNum();
		if (count <= 0) {
			throw new RuntimeException("zeor input");
		}
		Result<Integer> result = activity.brokenExchange(playerId, msg.getExchangeId(),
				count);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}

	@ProtocolHandler(code = HP.code.HERO_BACK_BUY_C_VALUE)
	public void onPlayerBuySupplyBox(HawkProtocol protocol, String playerId){
		HeroBackExchangeActivity activity = getActivity(ActivityType.HERO_BACK_EXCHANGE);
		Activity.HeroBackBuyReq req = protocol.parseProtocol(Activity.HeroBackBuyReq.getDefaultInstance());
		int chestId = req.getId();
		int count = req.getCount();
		if(count <= 0 || chestId <= 0){
			HawkLog.errPrintln("HeroBackBuyReq error, chestId:{}, countId:{}", chestId, count);
			return;
		}
		Result<?> result = activity.onPlayerBuyChest(chestId, count, playerId);
		if(result != null && result.isFail()){
			sendErrorAndBreak(playerId, HP.code.HERO_BACK_BUY_C_VALUE, result.getStatus());
		}
	}
}

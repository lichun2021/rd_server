package com.hawk.activity.type.impl.roulette;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.helper.HawkAssert;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.HPRouletteActivityItemSetReq;
import com.hawk.game.protocol.Activity.HPRouletteActivityLotteryReq;
import com.hawk.game.protocol.Activity.PandoraExchangeReq;
import com.hawk.game.protocol.Activity.PandoraLotteryReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

public class RouletteHandler extends ActivityProtocolHandler {
	
	/**
	 * 时空轮盘设置奖励
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code=HP.code.HP_ROULETTE_SET_ITEM_REQ_C_VALUE)
	public void setRewardItemReq(HawkProtocol protocol, String playerId){
		RouletteActivity activity = getActivity(ActivityType.ROULETTE_ACTIVITY);
		if(null != activity && activity.isOpening(playerId)){
			HPRouletteActivityItemSetReq proto = protocol.parseProtocol(HPRouletteActivityItemSetReq.getDefaultInstance());
			Result<?> result = activity.onProtocolSetRewardItem(playerId, proto);
			if(result.isFail()){
				sendErrorAndBreak(playerId, HP.code.HP_ROULETTE_SET_ITEM_REQ_C_VALUE, result.getStatus());
			}else{
				responseSuccess(playerId, protocol.getType());
			}
		}
	}
	
	@ProtocolHandler(code=HP.code.HP_ROULETTE_BOX_REWARD_REQ_C_VALUE)
	public void rewardBoxReq(HawkProtocol protocol, String playerId){
		RouletteActivity activity = getActivity(ActivityType.ROULETTE_ACTIVITY);
		if(null != activity && activity.isOpening(playerId)){
			//HPRouletteActivityItemSetReq proto = protocol.parseProtocol(HPRouletteActivityItemSetReq.getDefaultInstance());
			Result<?> result = activity.onProtocolRewardBox(playerId);
			if(result.isFail()){
				sendErrorAndBreak(playerId, HP.code.HP_ROULETTE_SET_ITEM_REQ_C_VALUE, result.getStatus());
			}else{
				responseSuccess(playerId, protocol.getType());
			}
		}
	}
	
	@ProtocolHandler(code=HP.code.HP_ROULETTE_LOTTERY_REQ_C_VALUE)
	public void lotteryReq(HawkProtocol protocol, String playerId){
		RouletteActivity activity = getActivity(ActivityType.ROULETTE_ACTIVITY);
		if(null != activity && activity.isOpening(playerId)){
			HPRouletteActivityLotteryReq proto = protocol.parseProtocol(HPRouletteActivityLotteryReq.getDefaultInstance());
			Result<?> result = activity.onProtocolLottery(playerId, proto);
			if(result.isFail()){
				sendErrorAndBreak(playerId, HP.code.HP_ROULETTE_SET_ITEM_REQ_C_VALUE, result.getStatus());
			}else{
				responseSuccess(playerId, protocol.getType());
			}
		}
	}
}

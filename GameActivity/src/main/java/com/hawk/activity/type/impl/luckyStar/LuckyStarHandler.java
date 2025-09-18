package com.hawk.activity.type.impl.luckyStar;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.luckyStarLotResponse;
import com.hawk.game.protocol.Activity.luckyStarLottery;
import com.hawk.game.protocol.Activity.luckyStarRecieveFreeBag;
import com.hawk.game.protocol.HP;

public class LuckyStarHandler extends ActivityProtocolHandler {
	
	@ProtocolHandler(code = HP.code.LUCKY_STAR_LOTTERY_VALUE)
	public void luckyStarPlayerLottery(HawkProtocol protocol, String playerId){
		LuckyStarActivity activity = getActivity(ActivityType.LUCKY_STAR);
		luckyStarLottery req = protocol.parseProtocol(luckyStarLottery.getDefaultInstance());
		int lotCnt = req.getLotCnt();
		Result<?> result = activity.lottery(playerId, lotCnt);
		if(result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return;
		}
		luckyStarLotResponse.Builder build = (luckyStarLotResponse.Builder)result.getRetObj();
		sendProtocol(playerId, HawkProtocol.valueOf(HP.code.LUCKY_STAR_LOTTERY_VALUE, build));
	}
	
	/***
	 * 领取幸运星免费宝箱奖励(每天限制领取一次)
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.LUCKY_STAR_RECIEVE_FREE_BAG_VALUE)
	public void luckyStarPlayerRecieveFreeBag(HawkProtocol protocol, String playerId){
		LuckyStarActivity activity = getActivity(ActivityType.LUCKY_STAR);
		luckyStarRecieveFreeBag req = protocol.parseProtocol(luckyStarRecieveFreeBag.getDefaultInstance());
		String bagId = req.getBagId();
		Result<?> result = activity.recieveFreeBag(playerId, bagId);
		if(result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
}

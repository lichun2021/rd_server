package com.hawk.activity.type.impl.playerComeBack;

import java.util.Optional;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.ComeBackPlayerBuyMsg;
import com.hawk.game.protocol.Activity.ComeBackPlayerExchangeMsg;
import com.hawk.game.protocol.Activity.ComeBackPlayerRecieveGreatReward;
import com.hawk.game.protocol.HP;

/***
 * 老玩家回归 C->S
 * @author yang.rao
 *
 */
public class ComeBackHandler extends ActivityProtocolHandler {
	
	// 1. 领取回归大礼
	@ProtocolHandler(code = HP.code.PLAYER_COME_BACK_RECIEVE_GREAT_REWARD_C_VALUE)
	public void comeBackPlayerRecieveReward(HawkProtocol protocol, String playerId){
		Optional<ActivityBase> optional =  ActivityManager.getInstance().getActivity(Activity.ActivityType.COME_BACK_PLAYER_GREAT_GIFT_VALUE);
		if (!optional.isPresent()){
			return;
		}
		ComeBackRewardActivity activity = (ComeBackRewardActivity)optional.get();
		ComeBackPlayerRecieveGreatReward proto = protocol.parseProtocol(ComeBackPlayerRecieveGreatReward.getDefaultInstance());
		Result<?> result = activity.onPlayerRecieveGreatGift(playerId, proto.getRewardId());
		if(result != null && result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
	
	// 2. 专属军资兑换
	@ProtocolHandler(code = HP.code.PLAYER_COME_BACK_EXCHANGE_C_VALUE)
	public void comeBackPlayerExchangeItems(HawkProtocol protocol, String playerId){
		Optional<ActivityBase> optional = ActivityManager.getInstance().getActivity(Activity.ActivityType.COME_BACK_PLAYER_EXCHANGE_VALUE);
		if (!optional.isPresent()){
			return;
		}
		ComeBackExchangeActivity activity = (ComeBackExchangeActivity)optional.get();
		ComeBackPlayerExchangeMsg proto = protocol.parseProtocol(ComeBackPlayerExchangeMsg.getDefaultInstance());
		Result<?> result = activity.onPlayerExchange(playerId, proto.getExchangeId(), proto.getNum());
		if(result != null && result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
		
	// 3. 低折回馈购买
	@ProtocolHandler(code = HP.code.PLAYER_COME_BACK_BUY_C_VALUE)
	public void comeBackPlayerDiscountBuy(HawkProtocol protocol, String playerId){
		Optional<ActivityBase> optional = ActivityManager.getInstance().getActivity(Activity.ActivityType.COME_BACK_PLAYER_DISCOUNT_BUY_VALUE);
		if (!optional.isPresent()){
			return;
		}
		ComeBackBuyActivity activity = (ComeBackBuyActivity)optional.get();
		ComeBackPlayerBuyMsg proto = protocol.parseProtocol(ComeBackPlayerBuyMsg.getDefaultInstance());
		Result<?> result = activity.onPlayerBuyChest(proto.getBuyId(), proto.getNum(), playerId);
		if(result != null && result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
}

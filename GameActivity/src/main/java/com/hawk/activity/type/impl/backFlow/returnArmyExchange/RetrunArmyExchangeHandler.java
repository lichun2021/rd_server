package com.hawk.activity.type.impl.backFlow.returnArmyExchange;

import java.util.Optional;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.ComeBackPlayerExchangeMsg;
import com.hawk.game.protocol.HP;

/***
 * 老玩家回归 C->S
 * @author yang.rao
 *
 */
public class RetrunArmyExchangeHandler extends ActivityProtocolHandler {
	
	/**
	 * 专属军资兑换
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.RETURN_ARMY_EXCHANGE_C_VALUE)
	public void comeBackPlayerExchangeItems(HawkProtocol protocol, String playerId){
		Optional<ActivityBase> optional = ActivityManager.getInstance().getActivity(Activity.ActivityType.RETURN_ARMY_EXCHANGE_VALUE);
		if (!optional.isPresent()){
			return;
		}
		ReturnArmyExchangeActivity activity = (ReturnArmyExchangeActivity)optional.get();
		ComeBackPlayerExchangeMsg proto = protocol.parseProtocol(ComeBackPlayerExchangeMsg.getDefaultInstance());
		Result<?> result = activity.onPlayerExchange(playerId, proto.getExchangeId(), proto.getNum());
		if(result != null && result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
		
}

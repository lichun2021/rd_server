package com.hawk.activity.type.impl.supergoldtwo;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.SuperGoldTwoInfo;
import com.hawk.game.protocol.Activity.SuperGoldTwoResult;
import com.hawk.game.protocol.HP;

public class SuperGoldTwoHandler extends ActivityProtocolHandler {
	
	/**  超级金矿2挖矿  **/
	@ProtocolHandler(code=HP.code.SUPER_GOLD_TWO_REQ_VALUE)
	public void superGold(HawkProtocol protocol, String playerId){
		SuperGoldTwoActivity activity = getActivity(ActivityType.SUPER_GOLD_TWO_ACTIVITY);
		Result<?> result = activity.onPlayerDigGold(playerId, protocol.getType());
		if(result == null){
			return;
		}
		if(result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return;
		}
		SuperGoldTwoResult.Builder ret = (SuperGoldTwoResult.Builder)result.getRetObj();
		sendProtocol(playerId, HawkProtocol.valueOf(HP.code.SUPER_GOLD_TWO_REQ_VALUE, ret));
	}
	
	@ProtocolHandler(code=HP.code.SUPER_GOLD_TWO_INFO_VALUE)
	public void superGoldInfo(HawkProtocol protocol, String playerId){
		SuperGoldTwoActivity activity = getActivity(ActivityType.SUPER_GOLD_TWO_ACTIVITY);
		Result<SuperGoldTwoInfo.Builder> result = activity.reqActivityInfo(playerId);
		if(result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return;
		}
		sendProtocol(playerId, HawkProtocol.valueOf(HP.code.SUPER_GOLD_TWO_INFO_VALUE, result.getRetObj()));
	}
}

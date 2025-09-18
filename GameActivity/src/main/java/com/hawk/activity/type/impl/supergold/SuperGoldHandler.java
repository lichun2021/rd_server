package com.hawk.activity.type.impl.supergold;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.SuperGoldInfo;
import com.hawk.game.protocol.Activity.SuperGoldResult;
import com.hawk.game.protocol.HP;

public class SuperGoldHandler extends ActivityProtocolHandler {
	
	/**  超级金矿挖矿  **/
	@ProtocolHandler(code=HP.code.SUPER_GOLD_REQ_VALUE)
	public void superGold(HawkProtocol protocol, String playerId){
		SuperGoldActivity activity = getActivity(ActivityType.SUPER_GOLD_ACTIVITY);
		Result<?> result = activity.onPlayerDigGold(playerId,protocol.getType());
		if(result == null){
			return;
		}
		if(result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return;
		}
		SuperGoldResult.Builder ret = (SuperGoldResult.Builder)result.getRetObj();
		sendProtocol(playerId, HawkProtocol.valueOf(HP.code.SUPER_GOLD_REQ_VALUE, ret));
	}
	
	@ProtocolHandler(code=HP.code.SUPER_GOLD_INFO_VALUE)
	public void superGoldInfo(HawkProtocol protocol, String playerId){
		SuperGoldActivity activity = getActivity(ActivityType.SUPER_GOLD_ACTIVITY);
		Result<SuperGoldInfo.Builder> result = activity.reqActivityInfo(playerId);
		if(result.isFail()){
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
			return;
		}
		sendProtocol(playerId, HawkProtocol.valueOf(HP.code.SUPER_GOLD_INFO_VALUE, result.getRetObj()));
	}
}

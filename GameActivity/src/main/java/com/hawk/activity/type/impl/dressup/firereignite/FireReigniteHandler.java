package com.hawk.activity.type.impl.dressup.firereignite;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

/**
 * 装扮投放系列活动三:重燃战火
 * @author hf
 */
public class FireReigniteHandler extends ActivityProtocolHandler {

	/***
	 * 兑换活动物品
	 */
	@ProtocolHandler(code = HP.code.FIRE_REIGNITE_EXCHANGE_REQ_VALUE)
	public void domeExchangePlayerTips(HawkProtocol protocol, String playerId) {
		Activity.FireReigniteExchangeReq req = protocol.parseProtocol(Activity.FireReigniteExchangeReq.getDefaultInstance());
		FireReigniteActivity activity = getActivity(ActivityType.FIRE_REIGNITE_ACTIVITY);
		if (activity == null){
			return;
		}
		Result<?> result = activity.exchangeFireReigniteItem(playerId,req.getIndex(), req.getNum());
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}

	/***
	 * 领取宝箱奖励
	 */
	@ProtocolHandler(code = HP.code.FIRE_REIGNITE_BOX_REQ_VALUE)
	public void receiveBoxReward(HawkProtocol protocol, String playerId) {
		FireReigniteActivity activity = getActivity(ActivityType.FIRE_REIGNITE_ACTIVITY);
		if (activity == null){
			return;
		}
		Result<Integer> result = activity.receiveBoxReward(playerId);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
}
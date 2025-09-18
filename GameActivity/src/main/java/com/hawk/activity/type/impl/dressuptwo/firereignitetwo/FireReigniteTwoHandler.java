package com.hawk.activity.type.impl.dressuptwo.firereignitetwo;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

/**
 * 圣诞节系列活动二:冬日装扮活动
 * @author hf
 */
public class FireReigniteTwoHandler extends ActivityProtocolHandler {

	/***
	 * 兑换活动物品
	 */
	@ProtocolHandler(code = HP.code.FIRE_REIGNITE_TWO_EXCHANGE_REQ_VALUE)
	public void domeExchangePlayerTips(HawkProtocol protocol, String playerId) {
		Activity.FireReigniteTwoExchangeReq req = protocol.parseProtocol(Activity.FireReigniteTwoExchangeReq.getDefaultInstance());
		FireReigniteTwoActivity activity = getActivity(ActivityType.FIRE_REIGNITE_TWO_ACTIVITY);
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
	@ProtocolHandler(code = HP.code.FIRE_REIGNITE_TWO_BOX_REQ_VALUE)
	public void receiveBoxReward(HawkProtocol protocol, String playerId) {
		FireReigniteTwoActivity activity = getActivity(ActivityType.FIRE_REIGNITE_TWO_ACTIVITY);
		if (activity == null){
			return;
		}
		Result<Integer> result = activity.receiveBoxReward(playerId);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
}
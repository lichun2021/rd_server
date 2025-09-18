package com.hawk.activity.type.impl.dressuptwo.gunpowderrisetwo;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

/**
 * 圣诞节系列活动三:冰雪商城活动
 * @author hf
 */
public class GunpowderRiseTwoHandler extends ActivityProtocolHandler {

	/***
	 * 前端兑换提醒勾勾
	 */
	@ProtocolHandler(code = HP.code.GUNPOWDER_RISE_TWO_TIPS_REQ_VALUE)
	public void domeExchangePlayerTips(HawkProtocol protocol, String playerId) {
		GunpowderRiseTwoActivity activity = getActivity(ActivityType.GUNPOWDER_RISE_TWO_ACTIVITY);
		Activity.GunpowderRiseTwoTipsReq req = protocol.parseProtocol(Activity.GunpowderRiseTwoTipsReq.getDefaultInstance());
		int id = req.getId();
		int tip = req.getTip();
		Result<?> result = activity.reqActivityTips(playerId, id, tip);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}

	/***
	 * 兑换接口
	 */
	@ProtocolHandler(code = HP.code.GUNPOWDER_RISE_TWO_EXCHANGE_REQ_VALUE)
	public void domeExchangeExchange(HawkProtocol protocol, String playerId) {
		GunpowderRiseTwoActivity activity = getActivity(ActivityType.GUNPOWDER_RISE_TWO_ACTIVITY);
		Activity.PBGunpowderRiseTwoExchange msg = protocol.parseProtocol(Activity.PBGunpowderRiseTwoExchange.getDefaultInstance());
		int count = msg.getNum();
		Result<Integer> result = activity.gunpowderRiseExchange(playerId, msg.getExchangeId(), count);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
}
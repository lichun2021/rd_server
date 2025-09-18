package com.hawk.activity.type.impl.dressup.gunpowderrise;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

/**
 * 装扮投放系列活动四:硝烟再起
 * @author hf
 */
public class GunpowderRiseHandler extends ActivityProtocolHandler {

	/***
	 * 前端兑换提醒勾勾
	 */
	@ProtocolHandler(code = HP.code.GUNPOWDER_RISE_TIPS_REQ_VALUE)
	public void domeExchangePlayerTips(HawkProtocol protocol, String playerId) {
		GunpowderRiseActivity activity = getActivity(ActivityType.GUNPOWDER_RISE_ACTIVITY);
		Activity.GunpowderRiseTipsReq req = protocol.parseProtocol(Activity.GunpowderRiseTipsReq.getDefaultInstance());
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
	@ProtocolHandler(code = HP.code.GUNPOWDER_RISE_EXCHANGE_REQ_VALUE)
	public void domeExchangeExchange(HawkProtocol protocol, String playerId) {
		GunpowderRiseActivity activity = getActivity(ActivityType.GUNPOWDER_RISE_ACTIVITY);
		Activity.PBGunpowderRiseExchange msg = protocol.parseProtocol(Activity.PBGunpowderRiseExchange.getDefaultInstance());
		int count = msg.getNum();
		Result<Integer> result = activity.gunpowderRiseExchange(playerId, msg.getExchangeId(), count);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}
	}
}
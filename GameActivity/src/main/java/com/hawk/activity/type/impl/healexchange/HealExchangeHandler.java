package com.hawk.activity.type.impl.healexchange;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.PBPBAllianceWishExchangeReq;
import com.hawk.game.protocol.HP;

public class HealExchangeHandler extends ActivityProtocolHandler {

	@ProtocolHandler(code = HP.code2.HEAL_ITEM_EXECHANGE_REQ_VALUE)
	public void allianceGuildExchange(HawkProtocol hawkProtocol, String playerId) {
		HealExchangeActivity activity = this.getActivity(ActivityType.HEAL_EXCHANGE_ACTIVITY);
		if (activity == null) {
			return;
		}
		PBPBAllianceWishExchangeReq req = hawkProtocol.parseProtocol(PBPBAllianceWishExchangeReq.getDefaultInstance());
		activity.itemExchange(playerId, req.getId(), req.getNum(), hawkProtocol.getType());
		activity.responseSuccess(playerId, hawkProtocol.getType());
	}

}

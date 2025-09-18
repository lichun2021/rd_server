package com.hawk.activity.type.impl.heroLove;


import java.util.List;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.HeroLoveGiftItemReq;
import com.hawk.game.protocol.Activity.HeroLoveReceiveAchievesReq;
import com.hawk.game.protocol.Activity.HeroLoveReceiveAchievesResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

/**
 * 委任英雄.
 * @author jm
 */
public class HeroLoveActivityHandler extends ActivityProtocolHandler {
	
	/**
	 * 进入活动界面
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code.HERO_LOVE_PAGE_INFO_REQ_VALUE)
	public boolean onEnterPage(HawkProtocol protocol, String playerId) {	
		HeroLoveActivity activity = getActivity(ActivityType.HERO_LOVE);		
		activity.syncActivityDataInfo(playerId);
		
		return true;
	}
	
	@ProtocolHandler(code = HP.code.HERO_LOVE_GIFT_ITEM_REQ_VALUE)
	public boolean onGiveItem(HawkProtocol protocol, String playerId) {
		HeroLoveGiftItemReq req = protocol.parseProtocol(HeroLoveGiftItemReq.getDefaultInstance());
		HeroLoveActivity activity = getActivity(ActivityType.HERO_LOVE);
		int result = activity.giveItem(playerId, req.getId(), req.getNum());
		if (result == Status.SysError.SUCCESS_OK_VALUE) {
			activity.syncActivityDataInfo(playerId);
		} else {
			this.sendErrorAndBreak(playerId, protocol.getType(), result);
		}
		return true;
	}
	
	@ProtocolHandler(code = HP.code.HERO_LOVE_RECEIVE_ACHIEVES_REQ_VALUE)
	public void onReceiveAchieves(HawkProtocol protocol, String playerId) {
		HeroLoveReceiveAchievesReq req = protocol.parseProtocol(HeroLoveReceiveAchievesReq.getDefaultInstance());
		HeroLoveActivity activity = getActivity(ActivityType.HERO_LOVE);
		List<Integer> receivedIdList = activity.receiveAchieves(playerId, req.getAchieveIdsList());
		if (receivedIdList != null && !receivedIdList.isEmpty()) {
			HeroLoveReceiveAchievesResp.Builder sbuilder = HeroLoveReceiveAchievesResp.newBuilder();
			sbuilder.addAllAchieveIds(receivedIdList);
			HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code.HERO_LOVE_RECEIVE_ACHIEVES_RESP_VALUE, sbuilder);
			this.sendProtocol(playerId, hawkProtocol);
		}
	}
}

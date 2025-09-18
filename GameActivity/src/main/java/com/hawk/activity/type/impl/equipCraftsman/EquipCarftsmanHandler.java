package com.hawk.activity.type.impl.equipCraftsman;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.EquipCraftsmanGachaReq;
import com.hawk.game.protocol.HP;

/**
 * 装备工匠
 * 
 * @author Golden
 *
 */
public class EquipCarftsmanHandler extends ActivityProtocolHandler {

	/***
	 * 请求界面信息
	 */
	@ProtocolHandler(code = HP.code.EQUIP_CRAFTSMAN_PAGE_INFO_REQ_VALUE)
	public void pageInfo(HawkProtocol protocol, String playerId) {
		EquipCarftsmanActivity activity = getActivity(ActivityType.EQUIP_CARFTSMAN_ACTIVITY);
		activity.syncPageInfo(playerId);
	}
	
	/***
	 * 抽取
	 */
	@ProtocolHandler(code = HP.code.EQUIP_CRAFTSMAN_GACHA_REQ_VALUE)
	public void gacha(HawkProtocol protocol, String playerId) {
		EquipCraftsmanGachaReq req = protocol.parseProtocol(EquipCraftsmanGachaReq.getDefaultInstance());
		EquipCarftsmanActivity activity = getActivity(ActivityType.EQUIP_CARFTSMAN_ACTIVITY);
		activity.gacha(playerId, req);
	}
	
	/***
	 * 兑换
	 */
	@ProtocolHandler(code = HP.code.EQUIP_CRAFTSMAN_EXCHANGE_VALUE)
	public void exchange(HawkProtocol protocol, String playerId) {
		EquipCarftsmanActivity activity = getActivity(ActivityType.EQUIP_CARFTSMAN_ACTIVITY);
		activity.clearABAttr(playerId);
	}
}

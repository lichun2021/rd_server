package com.hawk.activity.type.impl.equipBlackMarket;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.EquipBlackMarketRefinningReq;
import com.hawk.game.protocol.HP;

public class EquipBlackMarketActivityHandler extends ActivityProtocolHandler {
	
	
	/**
	 * 装备黑市炼化
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.EQUIP_BLACK_MARKET_INFO_REQ_VALUE)
	public void onEquipBlackMarketInfo(HawkProtocol protocol, String playerId){
		EquipBlackMarketActivity activity = getActivity(ActivityType.EQUIP_BLACK_MARKET_ACTIVITY);
		if(activity == null){
			return;
		}
		
		if(activity.isOpening(playerId)){
			activity.syncActivityDataInfo(playerId);
		}
		
	}

	/**
	 * 装备黑市炼化
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.EQUIP_BLACK_MARKET_REFINNING_REQ_VALUE)
	public void onEquipBlackMarketRefine(HawkProtocol protocol, String playerId){
		EquipBlackMarketActivity activity = getActivity(ActivityType.EQUIP_BLACK_MARKET_ACTIVITY);
		if(activity == null){
			return;
		}
		EquipBlackMarketRefinningReq req = protocol.parseProtocol(EquipBlackMarketRefinningReq
				.getDefaultInstance());
		int rId = req.getRefineId();
		int rCount = req.getCount();
		if(activity.isOpening(playerId)){
			activity.onMarketRefine(playerId, rId, rCount, protocol.getType());
		}
		
	}
}

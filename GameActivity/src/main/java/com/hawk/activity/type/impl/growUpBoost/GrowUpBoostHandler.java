package com.hawk.activity.type.impl.growUpBoost;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.GrowUpBoostExchangeReq;
import com.hawk.game.protocol.HP;

public class GrowUpBoostHandler extends ActivityProtocolHandler {
	
	
	/**
	 * 兑换
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.GROW_UP_BOOST_EXCHANGE_REQ_VALUE)
	public boolean onExchangeItem(HawkProtocol protocol, String playerId) {
		GrowUpBoostExchangeReq req = protocol.parseProtocol(GrowUpBoostExchangeReq.getDefaultInstance());
		GrowUpBoostActivity activity = getActivity(ActivityType.GROW_UP_BOOST);
		if(req.getType() == 1){
			activity.itemExchange(playerId, req.getCfgId(), req.getCount());			
		}
		if(req.getType() == 2){
			activity.itemBuy(playerId, req.getCfgId(), req.getCount());
		}
		return true;
	}
	
	
	
	/**
	 * 每日积分记录
	 * @param protocol
	 * @param playerId
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.GROW_UP_BOOST_DAILY_SCORE_RECORD_REQ_VALUE)
	public boolean onDailyScore(HawkProtocol protocol, String playerId) {
		GrowUpBoostActivity activity = getActivity(ActivityType.GROW_UP_BOOST);
		activity.syncDailyScoreRecord(playerId);
		return true;
	}
	
	
	
	

	
	
	
	
	
}

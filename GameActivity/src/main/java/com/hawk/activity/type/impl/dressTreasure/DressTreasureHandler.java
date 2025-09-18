package com.hawk.activity.type.impl.dressTreasure;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.DressTreasureExchangeReq;
import com.hawk.game.protocol.HP;

/**
 * 端午兑换
 * 
 * @author che
 *
 */
public class DressTreasureHandler extends ActivityProtocolHandler {
	
	
	@ProtocolHandler(code = HP.code2.DRESS_TREASURE_RANDOM_REQ_VALUE)
	public void dressTreasureRandom(HawkProtocol hawkProtocol, String playerId){
		DressTreasureActivity activity = this.getActivity(ActivityType.DRESS_TREASURE_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.randomAward(playerId);
	}
	
	
	@ProtocolHandler(code = HP.code2.DRESS_TREASURE_RESET_REQ_VALUE)
	public void dressTreasureRest(HawkProtocol hawkProtocol, String playerId){
		DressTreasureActivity activity = this.getActivity(ActivityType.DRESS_TREASURE_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.restRadomAward(playerId,hawkProtocol.getType());
	}
	
	
	@ProtocolHandler(code = HP.code2.DRESS_TREASURE_EXCHANGE_REQ_VALUE)
	public void dressTreasureExchange(HawkProtocol hawkProtocol, String playerId){
		DressTreasureActivity activity = this.getActivity(ActivityType.DRESS_TREASURE_ACTIVITY);
		if(activity == null){
			return;
		}
		DressTreasureExchangeReq req = hawkProtocol.parseProtocol(DressTreasureExchangeReq.getDefaultInstance());
		activity.itemExchange(playerId, req.getExchangeId(), req.getExchangeCount(), hawkProtocol.getType());
	}
	
	

	
	
}
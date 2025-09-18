package com.hawk.activity.type.impl.drogenBoatFestival.exchange;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.DragonBoatExchangeCareReq;
import com.hawk.game.protocol.Activity.DragonBoatExchangeReq;
import com.hawk.game.protocol.HP;

/**
 * 端午兑换
 * 
 * @author che
 *
 */
public class DragonBoatExchangeHandler extends ActivityProtocolHandler {
	
	
	@ProtocolHandler(code = HP.code.DRAGON_BOAT_EXCHANGE_CARE_REQ_VALUE)
	public void dragonBoatCare(HawkProtocol hawkProtocol, String playerId){
		DragonBoatExchangeActivity activity = this.getActivity(ActivityType.DRAGON_BOAT_EXCHANGE_ACTIVITY);
		if(activity == null){
			return;
		}
		DragonBoatExchangeCareReq req = hawkProtocol.parseProtocol(DragonBoatExchangeCareReq.getDefaultInstance());
		int exchangeType = req.getExchangeId();
		int care = req.getCare();
		boolean isAll = req.getIsAll();
		if(isAll){
			activity.exchangeAllCare(playerId, care, hawkProtocol.getType());
		}else {
			activity.exchageCare(playerId, exchangeType, care, hawkProtocol.getType());
		}
	}
	
	
	@ProtocolHandler(code = HP.code.DRAGON_BOAT_EXCHANGE_REQ_VALUE)
	public void dragonBoatExchange(HawkProtocol hawkProtocol, String playerId){
		DragonBoatExchangeActivity activity = this.getActivity(ActivityType.DRAGON_BOAT_EXCHANGE_ACTIVITY);
		if(activity == null){
			return;
		}
		DragonBoatExchangeReq req = hawkProtocol.parseProtocol(DragonBoatExchangeReq.getDefaultInstance());
		int exchangeType = req.getExchangeId();
		int count = req.getExchangeCount();
		activity.itemExchange(playerId, exchangeType, count,hawkProtocol.getType());
	}
	
	

	
	
}
package com.hawk.activity.type.impl.drogenBoatFestival.guildCelebration;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.DragonBoatCeletrationMakeGiftReq;
import com.hawk.game.protocol.HP;

/**
 * 时空豪礼消息处理
 * 
 * @author che
 *
 */
public class DragonBoatCelebrationHandler extends ActivityProtocolHandler {
	
	
	
	
	@ProtocolHandler(code = HP.code.DRAGON_BOAT_CELETRATION_INFO_REQ_VALUE)
	public void dragonBoatCelebrationDressTree(HawkProtocol hawkProtocol, String playerId){
		DragonBoatCelebrationActivity activity = this.getActivity(ActivityType.DRAGON_BOAT_CELERATION_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.syncActivityInfo(playerId);
		
	}
	
	
	@ProtocolHandler(code = HP.code.DRAGON_BOAT_CELETRATION_MAKE_GIFT_REQ_VALUE)
	public void dragonBoatCelebrationMakeGift(HawkProtocol hawkProtocol, String playerId){
		
		DragonBoatCeletrationMakeGiftReq req = hawkProtocol.
				parseProtocol(DragonBoatCeletrationMakeGiftReq.getDefaultInstance());
		
		int type = req.getType();
		int count = req.getCount();
		DragonBoatCelebrationActivity activity = this.getActivity(ActivityType.DRAGON_BOAT_CELERATION_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.makeGift(playerId,type,count,hawkProtocol.getType());
	}

	
	
	
	@ProtocolHandler(code = HP.code.DRAGON_BOAT_CELETRATION_RANK_REQ_VALUE)
	public void dragonBoatCelebrationRanks(HawkProtocol hawkProtocol, String playerId){
		DragonBoatCelebrationActivity activity = this.getActivity(ActivityType.DRAGON_BOAT_CELERATION_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.rankInfo(playerId,hawkProtocol.getType());
	}

	
	
}
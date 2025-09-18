package com.hawk.activity.type.impl.honourHeroBefell;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.PBHonourHeroBefellExchangeReq;
import com.hawk.game.protocol.Activity.PBHonourHeroBefellLotteryReq;
import com.hawk.game.protocol.Activity.PBHonourHeroBefellTipsReq;
import com.hawk.game.protocol.HP;

/**
 * 
 * @author che
 *
 */
public class HonourHeroBefellHandler extends ActivityProtocolHandler {
	
	
	@ProtocolHandler(code = HP.code2.HONOUR_HERO_BEFELL_LOTTERY_REQ_VALUE)
	public void lottery(HawkProtocol hawkProtocol, String playerId){
		HonourHeroBefellActivity activity = this.getActivity(ActivityType.HONOUR_HERO_BEFELL_ACTIVITY);
		if(activity == null){
			return;
		}
		PBHonourHeroBefellLotteryReq req = hawkProtocol.parseProtocol(PBHonourHeroBefellLotteryReq.getDefaultInstance());
		activity.lottery(playerId,req.getType());
	}
	
	
	@ProtocolHandler(code = HP.code2.HONOUR_HERO_BEFELL_TIP_REQ_VALUE)
	public void updateCare(HawkProtocol hawkProtocol, String playerId){
		HonourHeroBefellActivity activity = this.getActivity(ActivityType.HONOUR_HERO_BEFELL_ACTIVITY);
		if(activity == null){
			return;
		}
		PBHonourHeroBefellTipsReq req = hawkProtocol.parseProtocol(PBHonourHeroBefellTipsReq.getDefaultInstance());
		activity.updateActivityTips(playerId, req.getTipsList());
	}
	
	
	@ProtocolHandler(code = HP.code2.HONOUR_HERO_BEFELL_EXCHANGE_REQ_VALUE)
	public void itemExchange(HawkProtocol hawkProtocol, String playerId){
		HonourHeroBefellActivity activity = this.getActivity(ActivityType.HONOUR_HERO_BEFELL_ACTIVITY);
		if(activity == null){
			return;
		}
		PBHonourHeroBefellExchangeReq req = hawkProtocol.parseProtocol(PBHonourHeroBefellExchangeReq.getDefaultInstance());
		activity.itemExchange(playerId, req.getExchangeId(), req.getNum());
	}
	
	
	
}
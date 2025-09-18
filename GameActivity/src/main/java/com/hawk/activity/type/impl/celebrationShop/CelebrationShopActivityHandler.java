package com.hawk.activity.type.impl.celebrationShop;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.CelebrationShopExchangeReq;
import com.hawk.game.protocol.HP;

/**
 * 周年商城
 * @author luke
 */
public class CelebrationShopActivityHandler extends ActivityProtocolHandler {
	@ProtocolHandler(code = HP.code.CELEBRATION_SHOP_MAIN_REQ_VALUE)
	public void main(HawkProtocol hawkProtocol, String playerId){
		CelebrationShopActivity activity = this.getActivity(ActivityType.CELEBRATION_SHOP_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.syncActivityDataInfo(playerId);
	}
	
	@ProtocolHandler(code = HP.code.CELEBRATION_SHOP_EXCHANGE_REQ_VALUE)
	public void exchange(HawkProtocol hawkProtocol, String playerId){
		CelebrationShopActivity activity = this.getActivity(ActivityType.CELEBRATION_SHOP_ACTIVITY);
		if(activity == null){
			return;
		}
		CelebrationShopExchangeReq req = hawkProtocol.parseProtocol(CelebrationShopExchangeReq.getDefaultInstance());
		activity.exchange(playerId,req.getItemId(),req.getItemNum());
	}
}
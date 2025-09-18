package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 勋章投资礼包直购事件
 */
public class SupersoldierInvestGiftBuyEvent extends ActivityEvent {

	private String giftId;
	
	
	public SupersoldierInvestGiftBuyEvent(){ super(null);}
	public SupersoldierInvestGiftBuyEvent(String playerId, String giftId) {
		super(playerId, true);
		this.giftId = giftId;
	}

	public String getGiftId() {
		return giftId;
	}
}

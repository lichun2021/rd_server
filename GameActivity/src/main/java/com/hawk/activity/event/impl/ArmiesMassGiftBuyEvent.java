package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 勋章投资礼包直购事件
 */
public class ArmiesMassGiftBuyEvent extends ActivityEvent {

	private String giftId;
	
	
	public ArmiesMassGiftBuyEvent(){ super(null);}
	public ArmiesMassGiftBuyEvent(String playerId, String giftId) {
		super(playerId, true);
		this.giftId = giftId;
	}

	public String getGiftId() {
		return giftId;
	}
}

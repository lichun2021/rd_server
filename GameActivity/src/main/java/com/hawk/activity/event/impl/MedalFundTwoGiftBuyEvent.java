package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 勋章投资礼包2直购事件
 */
public class MedalFundTwoGiftBuyEvent extends ActivityEvent {

	private String giftId;
	
	
	public MedalFundTwoGiftBuyEvent(){ super(null);}
	public MedalFundTwoGiftBuyEvent(String playerId, String giftId) {
		super(playerId, true);
		this.giftId = giftId;
	}

	public String getGiftId() {
		return giftId;
	}
}

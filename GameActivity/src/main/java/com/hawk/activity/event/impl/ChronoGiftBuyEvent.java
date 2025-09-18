package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 时空豪礼直购事件
 * @author che
 *
 */
public class ChronoGiftBuyEvent extends ActivityEvent {

	private String giftId;
	
	
	public ChronoGiftBuyEvent(){ super(null);}
	public ChronoGiftBuyEvent(String playerId, String giftId) {
		super(playerId, true);
		this.giftId = giftId;
	}

	public String getGiftId() {
		return giftId;
	}
}

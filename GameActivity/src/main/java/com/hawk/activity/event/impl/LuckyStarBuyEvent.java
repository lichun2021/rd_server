package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 幸运星直购事件
 * @author yang.rao
 *
 */
public class LuckyStarBuyEvent extends ActivityEvent {

	private String giftId;
	
	
	public LuckyStarBuyEvent(){ super(null);}
	public LuckyStarBuyEvent(String playerId, String giftId) {
		super(playerId, true);
		this.giftId = giftId;
	}

	public String getGiftId() {
		return giftId;
	}
}

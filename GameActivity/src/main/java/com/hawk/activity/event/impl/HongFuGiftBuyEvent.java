package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 洪福礼包直购事件
 * @author hf
 */
public class HongFuGiftBuyEvent extends ActivityEvent {

	private String giftId;


	public HongFuGiftBuyEvent(){ super(null);}
	public HongFuGiftBuyEvent(String playerId, String giftId) {
		super(playerId, true);
		this.giftId = giftId;
	}

	public String getGiftId() {
		return giftId;
	}
}

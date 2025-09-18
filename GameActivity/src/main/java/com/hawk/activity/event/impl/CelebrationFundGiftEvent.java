package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;


/**
 * 周年庆庆典基金礼包
 * 
 */
public class CelebrationFundGiftEvent extends ActivityEvent {
	
	private static final long serialVersionUID = 1L;
	private String giftId;
	public CelebrationFundGiftEvent(){ super(null);}
	public CelebrationFundGiftEvent(String playerId,String giftId) {
		super(playerId, true);
		this.giftId = giftId;
	}
	
	public String getGiftId() {
		return giftId;
	}
}

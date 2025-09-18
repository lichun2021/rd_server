package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 空投补给活动礼包直购事件
 */
public class AirdropSupplyGiftBuyEvent extends ActivityEvent {

	private String giftId;
	
	
	public AirdropSupplyGiftBuyEvent(){ super(null);}
	public AirdropSupplyGiftBuyEvent(String playerId, String giftId) {
		super(playerId, true);
		this.giftId = giftId;
	}

	public String getGiftId() {
		return giftId;
	}
}

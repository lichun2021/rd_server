package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 勋章投资礼包直购事件
 */
public class StarInvestGiftBuyEvent extends ActivityEvent {

	private String giftId;
	private int rmb;
	
	public StarInvestGiftBuyEvent(){ super(null);}
	public StarInvestGiftBuyEvent(String playerId, String giftId,int rmb) {
		super(playerId, true);
		this.giftId = giftId;
		this.rmb = rmb;
	}

	public String getGiftId() {
		return giftId;
	}
	
	public int getRmb() {
		return rmb;
	}
}

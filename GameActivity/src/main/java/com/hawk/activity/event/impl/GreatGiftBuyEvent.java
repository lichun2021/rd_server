package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 超值好礼购买事件
 * @author yang.rao
 *
 */
public class GreatGiftBuyEvent extends ActivityEvent {

	private String giftId;
	
	public GreatGiftBuyEvent(){ super(null);}
	public GreatGiftBuyEvent(String playerId, String giftId) {
		super(playerId, true);
		this.giftId = giftId;
	}

	public String getGiftId() {
		return giftId;
	}

}

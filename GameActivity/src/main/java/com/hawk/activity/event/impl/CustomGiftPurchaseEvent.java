package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 定制礼包购买事件
 * 
 * @author lating
 *
 */
public class CustomGiftPurchaseEvent extends ActivityEvent {
	
	private String payGiftId;

	public CustomGiftPurchaseEvent(){ super(null);}
	public CustomGiftPurchaseEvent(String playerId, String payGiftId) {
		super(playerId, true);
		this.payGiftId = payGiftId;
	}

	public String getPayGiftId() {
		return payGiftId;
	}

}

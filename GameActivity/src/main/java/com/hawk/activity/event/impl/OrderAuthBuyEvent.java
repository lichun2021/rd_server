package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 定制礼包购买事件
 * 
 * @author jesse
 *
 */
public class OrderAuthBuyEvent extends ActivityEvent {
	
	private String payGiftId;

	public OrderAuthBuyEvent(){ super(null);}
	public OrderAuthBuyEvent(String playerId, String payGiftId) {
		super(playerId, true);
		this.payGiftId = payGiftId;
	}

	public String getPayGiftId() {
		return payGiftId;
	}

}

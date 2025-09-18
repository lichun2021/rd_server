package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 欢购豪礼礼包购买事件
 * 
 * @author lating
 *
 */
public class HappyGiftPurchaseEvent extends ActivityEvent {
	
	private String payGiftId;

	public HappyGiftPurchaseEvent(){ super(null);}
	public HappyGiftPurchaseEvent(String playerId, String payGiftId) {
		super(playerId, true);
		this.payGiftId = payGiftId;
	}

	public String getPayGiftId() {
		return payGiftId;
	}

}

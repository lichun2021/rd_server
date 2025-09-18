package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 先锋豪礼礼包购买事件
 * 
 * @author lating
 *
 */
public class PioneerGiftPurchaseEvent extends ActivityEvent {
	
	// 礼包档次
	private int type;

	public PioneerGiftPurchaseEvent(){ super(null);}
	public PioneerGiftPurchaseEvent(String playerId, int type) {
		super(playerId, true);
		this.type = type;
	}

	public int getType() {
		return type;
	}

}

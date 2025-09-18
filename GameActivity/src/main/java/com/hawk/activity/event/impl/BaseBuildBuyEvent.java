package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 基地飞升直购购买事件
 * 
 * @author jesse
 *
 */
public class BaseBuildBuyEvent extends ActivityEvent {
	
	private String payGiftId;

		
	public BaseBuildBuyEvent(){ super(null);}
	public BaseBuildBuyEvent(String playerId, String payGiftId) {
		super(playerId, true);
		this.payGiftId = payGiftId;
	}

	public String getPayGiftId() {
		return payGiftId;
	}

}

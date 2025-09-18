package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 战力基金购买事件
 * 
 * @author jesse
 *
 */
public class PowerFundBuyEvent extends ActivityEvent {
	
	private String payGiftId;

	public PowerFundBuyEvent(){ super(null);}
	public PowerFundBuyEvent(String playerId, String payGiftId) {
		super(playerId, true);
		this.payGiftId = payGiftId;
	}

	public String getPayGiftId() {
		return payGiftId;
	}

}

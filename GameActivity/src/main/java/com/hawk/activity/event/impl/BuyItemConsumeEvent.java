package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 购买每日必买礼包消耗
 * 
 * @author lating
 *
 */
public class BuyItemConsumeEvent extends ActivityEvent implements OrderEvent {
	
	private String giftId;

	private int costMoney;

	public BuyItemConsumeEvent(){ super(null);}
	public BuyItemConsumeEvent(String playerId,String giftId, int costMoney) {
		super(playerId, true);
		this.giftId = giftId;
		this.costMoney = costMoney;
	}

	public int getCostMoney() {
		return costMoney;
	}
	
	public String getGiftId() {
		return giftId;
	}
}

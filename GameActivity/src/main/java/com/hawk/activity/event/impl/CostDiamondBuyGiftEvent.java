package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 花费金条购买礼包
 * 
 * @author lating
 *
 */
public class CostDiamondBuyGiftEvent extends ActivityEvent {

	private int price;

	public CostDiamondBuyGiftEvent(){ super(null);}
	public CostDiamondBuyGiftEvent(String playerId, int price) {
		super(playerId);
		this.price = price;
	}

	public int getPrice() {
		return price;
	}

}

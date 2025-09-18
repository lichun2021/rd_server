package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 购买价格XX金条的超值礼包次数
 * 
 * @author lating
 *
 */
public class GiftPurchasePriceEvent extends ActivityEvent {

	private int price;
	private int times;

	public GiftPurchasePriceEvent(){ super(null);}
	public GiftPurchasePriceEvent(String playerId, int price, int times) {
		super(playerId);
		this.price = price;
		this.times = times;
	}

	public int getPrice() {
		return price;
	}

	public int getTimes() {
		return times;
	}

}

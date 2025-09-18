package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 双享豪礼购买天数
 * @author:Winder
 * @date:2020年8月20日
 */
public class DoubleGiftDayBuyEvent extends ActivityEvent {
	
	/** 累计登录天数*/
	private int buyDays;

	public DoubleGiftDayBuyEvent(){ super(null);}
	public DoubleGiftDayBuyEvent(String playerId, int buyDays) {
		super(playerId);
		this.buyDays = buyDays;
	}

	public int getBuyDays() {
		return buyDays;
	}

}

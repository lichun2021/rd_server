package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 特惠商人购买黑金礼包
 * @author che
 *
 */
public class TravelShopBuyBlackGoldPackageEvent extends ActivityEvent {

	private int num;
	
	public TravelShopBuyBlackGoldPackageEvent(){ super(null);}
	public TravelShopBuyBlackGoldPackageEvent(String playerId,int num) {
		super(playerId);
		this.num = num;
	}
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	
	
	
	

	

	
}

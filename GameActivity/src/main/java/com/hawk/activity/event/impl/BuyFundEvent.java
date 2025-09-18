package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 登录/成长基金购买事件
 * @author Jesse
 *
 */
public class BuyFundEvent extends ActivityEvent {
	private int activeType = 0;
	private int subType = 0;
	public BuyFundEvent(){ super(null);}
	public BuyFundEvent(String playerId, int activeType, int subType) {
		super(playerId);
		this.activeType = activeType;
		this.subType = subType;
	}

	public int getActiveType() {
		return activeType;
	}

	public int getSubType() {
		return subType;
	}
}

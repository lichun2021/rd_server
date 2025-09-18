package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 每日必买礼包物品收集数量
 * @author PhilChen
 *
 */
public class DailyBuyItemCountEvent extends ActivityEvent{

	private int count;
	
	
	public DailyBuyItemCountEvent(){ super(null);}
	public DailyBuyItemCountEvent(String playerId, int count) {
		super(playerId);
		this.count = count;
	}
	
	public int getCount() {
		return count;
	}
}

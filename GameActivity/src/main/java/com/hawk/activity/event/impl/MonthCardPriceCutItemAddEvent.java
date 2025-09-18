package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 特权礼包半价券道具增添事件
 * 
 * @author lating
 *
 */
public class MonthCardPriceCutItemAddEvent extends ActivityEvent {

	public MonthCardPriceCutItemAddEvent(){ super(null);}
	public MonthCardPriceCutItemAddEvent(String playerId) {
		super(playerId, true);
	}

}

package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 新春头奖专柜活动-抽奖消耗事件
 * 
 * @author lating
 * 
 */
public class BestPrizeDrawEvent extends ActivityEvent {
	
	private int drawConsume;

	public BestPrizeDrawEvent(){ super(null);}
	
	public BestPrizeDrawEvent(String playerId, int drawConsume) {
		super(playerId);
		this.drawConsume = drawConsume;
	}
	
	public int getDrawConsume() {
		return drawConsume;
	}
}

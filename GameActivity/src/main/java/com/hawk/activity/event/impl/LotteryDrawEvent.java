package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 十连抽活动抽奖事件
 * @author Jesse
 *
 */
public class LotteryDrawEvent extends ActivityEvent {

	private int times;
	
	public LotteryDrawEvent(){ super(null);}
	public LotteryDrawEvent(String playerId, int times) {
		super(playerId);
		this.times = times;
	}

	public int getTimes() {
		return times;
	}

}

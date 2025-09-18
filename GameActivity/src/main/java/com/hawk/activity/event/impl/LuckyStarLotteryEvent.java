package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 幸运星抽奖次数
 * 
 * @author lating
 * 
 */
public class LuckyStarLotteryEvent extends ActivityEvent {

	private int lotteryTotal;

	public LuckyStarLotteryEvent(){ super(null);}
	public LuckyStarLotteryEvent(String playerId, int lotteryTotal) {
		super(playerId);
		this.lotteryTotal = lotteryTotal;
	}
	
	public int getLotteryTotal() {
		return lotteryTotal;
	}
	
}

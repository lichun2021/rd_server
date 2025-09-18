package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 充值福利抽奖事件
 */
public class RechargeWelfareLotteryEvent extends ActivityEvent {
	private int count;

	public RechargeWelfareLotteryEvent(){ super(null);}
	public RechargeWelfareLotteryEvent(String playerId, int count) {
		super(playerId);
		this.count = count;
	}

	public int getCount() {
		return count;
	}

}

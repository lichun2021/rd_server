package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 机甲破世 打造机甲
 * 
 * @author RickMei
 *
 */
public class MachineSellEvent extends ActivityEvent {
	
	public int lotteryTimes = 0;

	public MachineSellEvent(){ super(null);}
	public MachineSellEvent(String playerId, int times) {
		super(playerId);
		this.lotteryTimes = times;
	}

	public static MachineSellEvent valueOf(String playerId, int times) {
		MachineSellEvent pbe = new MachineSellEvent(playerId, times);
		return pbe;
	}
}

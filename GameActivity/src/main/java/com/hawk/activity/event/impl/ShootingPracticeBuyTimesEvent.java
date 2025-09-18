package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 泰能超武投放活动中抽奖次数事件
 * 
 * @author lating
 * 
 */
public class ShootingPracticeBuyTimesEvent extends ActivityEvent {

	private int addTimes;

	public ShootingPracticeBuyTimesEvent(){ super(null);}
	
	public ShootingPracticeBuyTimesEvent(String playerId, int addTimes) {
		super(playerId);
		this.addTimes = addTimes;
	}
	
	public int getAddTimes() {
		return addTimes;
	}
	
}

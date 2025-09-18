package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 泰能超武投放活动中抽奖次数事件
 * 
 * @author lating
 * 
 */
public class PlantWeaponDrawTimesEvent extends ActivityEvent {

	private int addTimes;

	public PlantWeaponDrawTimesEvent(){ super(null);}
	
	public PlantWeaponDrawTimesEvent(String playerId, int addTimes) {
		super(playerId);
		this.addTimes = addTimes;
	}
	
	public int getAddTimes() {
		return addTimes;
	}
	
}

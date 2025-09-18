package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 超武返场活动抽奖次数事件
 * 
 * @author lating
 * 
 */
public class PlantWeaponBackDrawEvent extends ActivityEvent {
	
	private int times;

	public PlantWeaponBackDrawEvent(){ super(null);}
	
	public PlantWeaponBackDrawEvent(String playerId, int times) {
		super(playerId);
		this.times = times;
	}
	
	public int getTimes() {
		return times;
	}
}

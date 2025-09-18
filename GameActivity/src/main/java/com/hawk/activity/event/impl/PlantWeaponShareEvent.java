package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 泰能超武投放活动中，分享泰能超武事件
 * 
 * @author lating
 * 
 */
public class PlantWeaponShareEvent extends ActivityEvent {

	public PlantWeaponShareEvent(){ super(null);}
	
	public PlantWeaponShareEvent(String playerId) {
		super(playerId);
	}
}

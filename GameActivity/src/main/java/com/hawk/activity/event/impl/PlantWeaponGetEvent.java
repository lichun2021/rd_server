package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 泰能超武投放活动中，获得泰能超武事件
 * 
 * @author lating
 * 
 */
public class PlantWeaponGetEvent extends ActivityEvent {
	
	private int plantWeaponId;

	public PlantWeaponGetEvent(){ super(null);}
	
	public PlantWeaponGetEvent(String playerId, int plantWeaponId) {
		super(playerId);
		this.plantWeaponId = plantWeaponId;
	}
	
	public int getPlantWeaponId() {
		return plantWeaponId;
	}
}

package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class DragonBoatGiftAchieveEvent extends ActivityEvent {

	private long boatId;
	
	public DragonBoatGiftAchieveEvent(){ super(null);}
	public DragonBoatGiftAchieveEvent(String playerId,long boatId) {
		super(playerId);
		this.boatId = boatId;
	}

	public long getBoatId() {
		return boatId;
	}

	public void setBoatId(long boatId) {
		this.boatId = boatId;
	}

	

	
}

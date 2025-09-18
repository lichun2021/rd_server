package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class DragonBoatRefreshEvent extends ActivityEvent {

	private long boatId;
	public DragonBoatRefreshEvent(){ super(null);}
	public DragonBoatRefreshEvent(String playerId,long boatId) {
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

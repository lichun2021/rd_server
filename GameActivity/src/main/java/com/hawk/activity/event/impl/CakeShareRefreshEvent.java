package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class CakeShareRefreshEvent extends ActivityEvent {

	private long cakeId;
	
	public CakeShareRefreshEvent(){ super(null);}
	public CakeShareRefreshEvent(String playerId,long cakeId) {
		super(playerId);
		this.cakeId = cakeId;
	}

	public long getCakeId() {
		return cakeId;
	}

	public void setCakeId(long cakeId) {
		this.cakeId = cakeId;
	}

}

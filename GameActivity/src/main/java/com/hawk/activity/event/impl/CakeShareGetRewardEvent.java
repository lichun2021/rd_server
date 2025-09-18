package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class CakeShareGetRewardEvent extends ActivityEvent {

	private long cakeId;
	
	public CakeShareGetRewardEvent(){ super(null);}
	public CakeShareGetRewardEvent(String playerId,long cakeId) {
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

package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class DressActiveEvent extends ActivityEvent {
    
	private int dressId;
	
    public DressActiveEvent() { 
    	super(null);
    }
    
    public DressActiveEvent(String playerId, int dressId) {
        super(playerId);
        this.dressId = dressId;
    }

	public int getDressId() {
		return dressId;
	}
}

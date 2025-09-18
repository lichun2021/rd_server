package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class OverlordBlessingEvent extends ActivityEvent {
    
	private int num;
	
    public OverlordBlessingEvent() { 
    	super(null);
    }
    
    public OverlordBlessingEvent(String playerId, int num) {
        super(playerId);
        this.num = num;
    }

    public int getNum() {
        return num;
    }
}

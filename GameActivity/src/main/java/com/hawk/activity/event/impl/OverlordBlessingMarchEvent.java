package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class OverlordBlessingMarchEvent extends ActivityEvent {
    
	public OverlordBlessingMarchEvent() { 
		super(null);
	}
    
	public OverlordBlessingMarchEvent(String playerId) {
        super(playerId, true);
    }

}

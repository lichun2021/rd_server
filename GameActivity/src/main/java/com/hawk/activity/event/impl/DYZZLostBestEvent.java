package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class DYZZLostBestEvent extends ActivityEvent {
	public DYZZLostBestEvent(){ super(null);}
    public DYZZLostBestEvent(String playerId) {
        super(playerId);
    }
}

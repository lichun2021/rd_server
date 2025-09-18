package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class DYZZWinBestEvent extends ActivityEvent {
	public DYZZWinBestEvent(){ super(null);}
    public DYZZWinBestEvent(String playerId) {
        super(playerId);
    }
}

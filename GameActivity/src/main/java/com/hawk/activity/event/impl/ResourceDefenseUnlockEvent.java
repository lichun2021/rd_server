package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class ResourceDefenseUnlockEvent extends ActivityEvent {

	public ResourceDefenseUnlockEvent(){ super(null);}
	public ResourceDefenseUnlockEvent(String playerId) {
		super(playerId, true);
	}
}

package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class MonthCardSignOK extends ActivityEvent {
	
	public MonthCardSignOK(){ super(null);}
	private MonthCardSignOK(String playerId) {
		super(playerId);
	}
	
	public static MonthCardSignOK valueOf(String playerId) {
		MonthCardSignOK event = new MonthCardSignOK(playerId);
		return event;
	}
}

package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.ActivityEvent;

public class GoldBabyFindTimesEvent extends ActivityEvent{
	
	private static final long serialVersionUID = 1L;
	private int times;
	public GoldBabyFindTimesEvent(){ super(null);}
	public GoldBabyFindTimesEvent(String playerId,int times) {
		super(playerId);
		this.times = times;
	}
	
	public int getTimes() {
		return times;
	}
}

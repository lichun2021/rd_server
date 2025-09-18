package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class GoldBabyOnlineTimeEvent extends ActivityEvent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long lastLoginTime;
	public GoldBabyOnlineTimeEvent(){ super(null);}
	public GoldBabyOnlineTimeEvent(String playerId,long lastLoginTime) {
		super(playerId);
		this.lastLoginTime=lastLoginTime;
	}
	public long getLastLoginTime() {
		return lastLoginTime;
	}
}

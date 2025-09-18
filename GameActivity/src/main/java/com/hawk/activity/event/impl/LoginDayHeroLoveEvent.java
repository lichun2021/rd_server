package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class LoginDayHeroLoveEvent extends ActivityEvent {
	private int loginDays;
	public LoginDayHeroLoveEvent(){ super(null);}
	public LoginDayHeroLoveEvent(String playerId, int loginDays) {
		super(playerId);
		this.loginDays = loginDays;
	}
	
	public int getLoginDays() {
		return loginDays;
	}

}

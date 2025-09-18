package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class LoginDayDestinyRevolverEvent extends ActivityEvent {
	
	/** 累计登录天数*/
	private int loginDays;

	public LoginDayDestinyRevolverEvent(){ super(null);}
	public LoginDayDestinyRevolverEvent(String playerId, int loginDays) {
		super(playerId);
		this.loginDays = loginDays;
	}
	
	public int getLoginDays() {
		return loginDays;
	}
}

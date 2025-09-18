package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class LoginDayJoyBuyEvent extends ActivityEvent {
	
	/** 累计登录天数*/
	private int loginDays;

	public LoginDayJoyBuyEvent(){ super(null);}
	public LoginDayJoyBuyEvent(String playerId, int loginDays) {
		super(playerId);
		this.loginDays = loginDays;
	}
	
	public int getLoginDays() {
		return loginDays;
	}
}

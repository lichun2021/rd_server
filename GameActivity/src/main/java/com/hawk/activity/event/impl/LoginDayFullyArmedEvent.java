package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 登录事件(登录基金)
 * @author Jesse
 *
 */
public class LoginDayFullyArmedEvent extends ActivityEvent {
	
	/** 累计登录天数*/
	private int loginDays;

	public LoginDayFullyArmedEvent(){ super(null);}
	public LoginDayFullyArmedEvent(String playerId, int loginDays) {
		super(playerId);
		this.loginDays = loginDays;
	}
	
	public int getLoginDays() {
		return loginDays;
	}
}

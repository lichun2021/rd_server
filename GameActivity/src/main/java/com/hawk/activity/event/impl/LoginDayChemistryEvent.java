package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 登录事件(再续前缘)
 * @author Jesse
 *
 */
public class LoginDayChemistryEvent extends ActivityEvent {
	
	/** 累计登录天数*/
	private int loginDays;

	public LoginDayChemistryEvent(){ super(null);}
	public LoginDayChemistryEvent(String playerId, int loginDays) {
		super(playerId);
		this.loginDays = loginDays;
	}
	
	public int getLoginDays() {
		return loginDays;
	}
}

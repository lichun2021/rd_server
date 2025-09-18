package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
/**
 * 军事战备登录
 * @author Winder
 *
 */
public class LoginDaysActivityEvent extends ActivityEvent {
	
	/** 累计登录天数*/
	private int loginDays;

	public LoginDaysActivityEvent(){ super(null);}
	public LoginDaysActivityEvent(String playerId, int loginDays, int activityId) {
		super(playerId);
		this.loginDays = loginDays;
		setActivityType(activityId);
	}
	
	public int getLoginDays() {
		return loginDays;
	}
}

package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 登录事件(预流失干预活动)
 * 
 * @author lating
 *
 */
public class LoginDayPrestressLossEvent extends ActivityEvent {
	
	/** 累计登录天数*/
	private int loginDays;

	public LoginDayPrestressLossEvent(){ super(null);}
	public LoginDayPrestressLossEvent(String playerId, int loginDays) {
		super(playerId);
		this.loginDays = loginDays;
	}
	
	public int getLoginDays() {
		return loginDays;
	}
}

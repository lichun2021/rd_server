package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 登录事件(累计登录活动,按注册时间开启)
 * @author Jesse
 *
 */
public class LoginDayEvent extends ActivityEvent {
	
	/** 累计登录天数*/
	private int loginDays;

	public LoginDayEvent(){ super(null);}
	public LoginDayEvent(String playerId, int loginDays) {
		super(playerId);
		this.loginDays = loginDays;
	}
	
	public int getLoginDays() {
		return loginDays;
	}
}

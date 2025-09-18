package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 登录事件(战地福利)
 * @author Jesse
 *
 */
public class LoginDayLuckyWelfareEvent extends ActivityEvent {
	
	/** 累计登录天数*/
	private int loginDays;

	public LoginDayLuckyWelfareEvent(){ super(null);}
	public LoginDayLuckyWelfareEvent(String playerId, int loginDays) {
		super(playerId);
		this.loginDays = loginDays;
	}
	
	public int getLoginDays() {
		return loginDays;
	}
}

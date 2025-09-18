package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**登录事件(祝福语活动)
 * hf
 */
public class LoginDayGreetingsEvent extends ActivityEvent {
	
	/** 累计登录天数*/
	private int loginDays;

	public LoginDayGreetingsEvent(){ super(null);}
	public LoginDayGreetingsEvent(String playerId, int loginDays) {
		super(playerId);
		this.loginDays = loginDays;
	}
	
	public int getLoginDays() {
		return loginDays;
	}
}

package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 双十一拼图活动登录
 * @author hf
 *
 */
public class LoginDayJigsawConnectEvent extends ActivityEvent {

	/** 累计登录天数*/
	private int loginDays;

	public LoginDayJigsawConnectEvent(){ super(null);}
	public LoginDayJigsawConnectEvent(String playerId, int loginDays) {
		super(playerId);
		this.loginDays = loginDays;
	}
	
	public int getLoginDays() {
		return loginDays;
	}
}

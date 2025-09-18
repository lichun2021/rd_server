package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
/**
 * 空投补给登录
 * @author Winder
 *
 */
public class LoginDayAirdropEvent extends ActivityEvent {
	
	/** 累计登录天数*/
	private int loginDays;

	public LoginDayAirdropEvent(){ super(null);}
	public LoginDayAirdropEvent(String playerId, int loginDays) {
		super(playerId);
		this.loginDays = loginDays;
	}
	
	public int getLoginDays() {
		return loginDays;
	}
}

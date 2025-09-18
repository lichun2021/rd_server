package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;


/**中秋庆典登录事件
 * @author Winder
 *
 */
public class LoginDayMidAutumnEvent extends ActivityEvent {
	
	/** 累计登录天数*/
	private int loginDays;

	public LoginDayMidAutumnEvent(){ super(null);}
	public LoginDayMidAutumnEvent(String playerId, int loginDays) {
		super(playerId);
		this.loginDays = loginDays;
	}
	
	public int getLoginDays() {
		return loginDays;
	}
}

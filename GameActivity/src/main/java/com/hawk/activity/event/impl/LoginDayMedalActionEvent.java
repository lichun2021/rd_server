package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**登录事件(勋章行动)
 * @author Winder

 */
public class LoginDayMedalActionEvent extends ActivityEvent {
	
	/** 累计登录天数*/
	private int loginDays;

	public LoginDayMedalActionEvent(){ super(null);}
	public LoginDayMedalActionEvent(String playerId, int loginDays) {
		super(playerId);
		this.loginDays = loginDays;
	}
	
	public int getLoginDays() {
		return loginDays;
	}
}

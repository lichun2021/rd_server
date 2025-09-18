package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 豪礼派送累计登录事件
 * @author yang.rao
 *
 */
public class LoginDayGiftSendEvent extends ActivityEvent {

	/** 累计登录天数*/
	private int loginDays;
	
	public LoginDayGiftSendEvent(){ super(null);}
	public LoginDayGiftSendEvent(String playerId, int loginDays) {
		super(playerId);
		this.loginDays = loginDays;
	}

	public int getLoginDays() {
		return loginDays;
	}
}

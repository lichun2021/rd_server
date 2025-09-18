package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 老玩家回归，累计登录的天数
 * @author yang.rao
 */

public class ComeBackLoginDayEvent extends ActivityEvent {
	
	/** 累计登录天数*/
	private int loginDays;

	public ComeBackLoginDayEvent(){ super(null);}
	public ComeBackLoginDayEvent(String playerId, int loginDays) {
		super(playerId);
		this.loginDays = loginDays;
	}
	
	public int getLoginDays() {
		return loginDays;
	}
}

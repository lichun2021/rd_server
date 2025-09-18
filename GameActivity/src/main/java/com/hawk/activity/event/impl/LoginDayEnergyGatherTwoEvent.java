package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 冰雪计划
 * @author hf
 *
 */
public class LoginDayEnergyGatherTwoEvent extends ActivityEvent {

	/** 累计登录天数*/
	private int loginDays;

	public LoginDayEnergyGatherTwoEvent(){ super(null);}
	public LoginDayEnergyGatherTwoEvent(String playerId, int loginDays) {
		super(playerId);
		this.loginDays = loginDays;
	}
	
	public int getLoginDays() {
		return loginDays;
	}
}

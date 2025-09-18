package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 登录事件(装扮投放系列活动二:能量聚集)
 * @author hf
 *
 */
public class LoginDayEnergyGatherEvent extends ActivityEvent {

	/** 累计登录天数*/
	private int loginDays;

	public LoginDayEnergyGatherEvent(){ super(null);}
	public LoginDayEnergyGatherEvent(String playerId, int loginDays) {
		super(playerId);
		this.loginDays = loginDays;
	}
	
	public int getLoginDays() {
		return loginDays;
	}
}

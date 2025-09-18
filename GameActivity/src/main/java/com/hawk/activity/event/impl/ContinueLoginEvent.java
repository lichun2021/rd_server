package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 连续登录事件，玩家每天首次上线或玩家在线时跨24点都会产生该事件
 * @author PhilChen
 *
 */
public class ContinueLoginEvent extends ActivityEvent {
	
	/** 累计登录天数*/
	private int loginDays;
	
	/** 连续登录天数*/
	private int continueLoginDays;
	
	private boolean isLogin;
	
	/** 是否跨天*/
	private boolean isCrossDay;

	public ContinueLoginEvent(){ super(null);}
	public ContinueLoginEvent(String playerId, int loginDays, int continueLoginDays, boolean isLogin, boolean isCrossDay) {
		super(playerId,true);
		this.loginDays = loginDays;
		this.continueLoginDays = continueLoginDays;
		this.isLogin = isLogin;
		this.isCrossDay = isCrossDay;
	}
	
	public int getLoginDays() {
		return loginDays;
	}
	
	public int getContinueLoginDays() {
		return continueLoginDays;
	}
	
	public boolean isLogin() {
		return isLogin;
	}

	public boolean isCrossDay() {
		return isCrossDay;
	}

	public void setLoginDays(int loginDays) {
		this.loginDays = loginDays;
	}

	public void setContinueLoginDays(int continueLoginDays) {
		this.continueLoginDays = continueLoginDays;
	}

	public void setLogin(boolean isLogin) {
		this.isLogin = isLogin;
	}

	public void setCrossDay(boolean isCrossDay) {
		this.isCrossDay = isCrossDay;
	}
	
}

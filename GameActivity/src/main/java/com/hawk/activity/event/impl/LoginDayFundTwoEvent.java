package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 登录事件(登录基金2)
 * @author hf
 *
 */
public class LoginDayFundTwoEvent extends ActivityEvent {

	public LoginDayFundTwoEvent(){ super(null);}
	public LoginDayFundTwoEvent(String playerId) {
		super(playerId);
	}
	
}

package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 首充事件
 * @author golden
 *
 */
public class FirstRechargeEvent extends ActivityEvent {
	
	public FirstRechargeEvent(){ super(null);}
	public FirstRechargeEvent(String playerId) {
		super(playerId, true);
	}
	
	@Override
	public boolean isSkip() {
		return true;
	}
}

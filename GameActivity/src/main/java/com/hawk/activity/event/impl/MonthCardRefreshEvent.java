package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 跨天刷新月卡状态
 * 
 * @author lating
 *
 */
public class MonthCardRefreshEvent extends ActivityEvent {
	
	public MonthCardRefreshEvent(){ super(null);}
	public MonthCardRefreshEvent(String playerId) {
		super(playerId);
	}
	
}

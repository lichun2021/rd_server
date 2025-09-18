package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 情报中心打怪【清除危险】事件
 * 
 * @author lating
 * 
 */
public class AgencyMonsterAtkWinEvent extends ActivityEvent {

	private int times;

	public AgencyMonsterAtkWinEvent(){ super(null);}
	public AgencyMonsterAtkWinEvent(String playerId, int times) {
		super(playerId);
		this.times = times;
	}
	
	public int getTimes() {
		return times;
	}
	
}

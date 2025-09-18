package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 赛后庆典致敬次数刷新事件
 * @author lating
 */
public class AfterCompetitionHomageRefreshEvent extends ActivityEvent {

	private int homageVal;
	
	public AfterCompetitionHomageRefreshEvent(){ super(null);}
	public AfterCompetitionHomageRefreshEvent(String playerId, int homageVal) {
		super(playerId);
		this.homageVal = homageVal;
	}
	
	public int getHomageVal() {
		return homageVal;
	}
	
	public void setHomageVal(int homageVal) {
		this.homageVal = homageVal;
	}
}

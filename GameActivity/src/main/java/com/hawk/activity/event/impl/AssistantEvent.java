package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 士兵援助事件
 * 
 * @author lating
 *
 */
public class AssistantEvent extends ActivityEvent {
	
	private int soldierCnt;
	
	public AssistantEvent(){ super(null);}
	public AssistantEvent(String playerId, int soldierCnt) {
		super(playerId);
		this.soldierCnt = soldierCnt;
	}

	public int getSoldierCnt() {
		return soldierCnt;
	}
	
	@Override
	public boolean isOfflineResent() {
		return true;
	}
}

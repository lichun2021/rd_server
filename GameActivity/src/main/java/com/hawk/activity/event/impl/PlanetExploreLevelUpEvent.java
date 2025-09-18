package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 星能探索养成线升级事件
 * 
 * @author lating
 * 
 */
public class PlanetExploreLevelUpEvent extends ActivityEvent {

	private int times;

	public PlanetExploreLevelUpEvent(){ super(null);}
	public PlanetExploreLevelUpEvent(String playerId, int times) {
		super(playerId);
		this.times = times;
	}
	
	public int getTimes() {
		return times;
	}
	
}

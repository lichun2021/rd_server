package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 治疗伤兵事件
 * @author PhilChen
 *
 */
public class TreatArmyEvent extends ActivityEvent {

	private int count;
	
	public TreatArmyEvent(){ super(null);}
	public TreatArmyEvent(String playerId, int count) {
		super(playerId);
		this.count = count;
	}
	
	public int getCount() {
		return count;
	}

}

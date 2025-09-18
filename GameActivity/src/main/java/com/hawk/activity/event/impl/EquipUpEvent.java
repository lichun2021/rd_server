package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 装备打造/升阶/升品
 * @author golden
 *
 */
public class EquipUpEvent extends ActivityEvent {
	
	private int count;
	
	public EquipUpEvent(){ super(null);}
	public EquipUpEvent(String playerId, int count) {
		super(playerId);
		this.count = count;
	}

	public int getCount() {
		return count;
	}
}

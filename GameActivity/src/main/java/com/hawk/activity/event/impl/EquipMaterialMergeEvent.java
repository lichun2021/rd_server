package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 装备合成
 * @author golden
 *
 */
public class EquipMaterialMergeEvent extends ActivityEvent {

	private int count;
	
	public EquipMaterialMergeEvent(){ super(null);}
	public EquipMaterialMergeEvent(String playerId, int count) {
		super(playerId);
		this.count = count;
	}

	public int getCount() {
		return count;
	}
}

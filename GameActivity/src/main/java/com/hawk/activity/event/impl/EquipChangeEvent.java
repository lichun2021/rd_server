package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 装备数量/品质变更事件
 * 
 * @author Jesse
 *
 */
public class EquipChangeEvent extends ActivityEvent {
	
	public EquipChangeEvent(){ super(null);}
	public EquipChangeEvent(String playerId) {
		super(playerId);
	}
}

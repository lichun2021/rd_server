package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 装备等级提升事件
 * @author PhilChen
 *
 */
public class ArmourStrengthenEvent extends ActivityEvent {
	
	private int armourId;
	/** 当前等级*/
	private int level;

	public ArmourStrengthenEvent(){ super(null);}
	public ArmourStrengthenEvent(String playerId, int armourId, int level) {
		super(playerId);
		this.armourId = armourId;
		this.level = level;
	}
	
	public int getArmourId() {
		return armourId;
	}
	
	public int getLevel() {
		return level;
	}

}

package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 贵族等级提升事件
 * @author PhilChen
 *
 */
public class VipLevelupEvent extends ActivityEvent {
	
	/** vip当前等级*/
	private int level;

	private int oldLevel;
	
	public VipLevelupEvent(){ super(null);}
	public VipLevelupEvent(String playerId, int oldLevel, int level) {
		super(playerId);
		this.level = level;
		this.oldLevel = oldLevel;
	}
	
	public int getOldLevel() {
		return oldLevel;
	}
	
	public int getLevel() {
		return level;
	}

}

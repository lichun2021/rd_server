package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 指挥官等级提升事件
 * @author PhilChen
 *
 */
public class PlayerLevelUpEvent extends ActivityEvent {
	
	/** 当前等级*/
	private int level;

	public PlayerLevelUpEvent(){ super(null);}
	public PlayerLevelUpEvent(String playerId, int level) {
		super(playerId);
		this.level = level;
	}
	
	public int getLevel() {
		return level;
	}

}

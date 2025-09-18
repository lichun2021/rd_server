package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 英雄等级提升事件
 * @author PhilChen
 *
 */
public class HeroLevelUpEvent extends ActivityEvent {
	
	private int heroId;
	/** 当前等级*/
	private int level;

	public HeroLevelUpEvent(){ super(null);}
	public HeroLevelUpEvent(String playerId, int heroId, int level) {
		super(playerId);
		this.heroId = heroId;
		this.level = level;
	}
	
	public int getHeroId() {
		return heroId;
	}
	
	public int getLevel() {
		return level;
	}

}

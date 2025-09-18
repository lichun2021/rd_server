package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

public class HeroLevelUpMsg extends HawkMsg {
	/**
	 * 英雄配置ID
	 */
	private int heroId;
	/**
	 * 上一次的等级
	 */
	private int oldLevel;
	/**
	 * 最新的等级
	 */
	private int newLevel;
	
	public HeroLevelUpMsg(int heroId, int oldLevel, int newLevel) {
		this.heroId = heroId;
		this.oldLevel = oldLevel;
		this.newLevel = newLevel;
	}
	
	public int getHeroId() {
		return heroId;
	}
	public void setHeroId(int heroId) {
		this.heroId = heroId;
	}
	public int getOldLevel() {
		return oldLevel;
	}
	public void setOldLevel(int oldLevel) {
		this.oldLevel = oldLevel;
	}
	public int getNewLevel() {
		return newLevel;
	}
	public void setNewLevel(int newLevel) {
		this.newLevel = newLevel;
	}
	
	
}

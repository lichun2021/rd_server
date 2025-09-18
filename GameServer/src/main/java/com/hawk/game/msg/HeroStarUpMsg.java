package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

public class HeroStarUpMsg extends HawkMsg {
	/**
	 * 英雄的配置ID 
	 */
	private int heroId;
	/**
	 * 原来的星级
	 */
	private int oldStar;
	/**
	 * 新的星级
	 */
	private int newStar;
	
	public HeroStarUpMsg(int heroId, int oldStar, int newStar) {
		this.heroId = heroId;
		this.oldStar = oldStar;
		this.newStar = newStar;
	}

	public int getOldStar() {
		return oldStar;
	}

	public void setOldStar(int oldStar) {
		this.oldStar = oldStar;
	}

	public int getNewStar() {
		return newStar;
	}

	public void setNewStar(int newStar) {
		this.newStar = newStar;
	}

	public int getHeroId() {
		return heroId;
	}

	public void setHeroId(int heroId) {
		this.heroId = heroId;
	}
	
	
}

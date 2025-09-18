package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class HeroUpStarEvent extends ActivityEvent {
	/**英雄id*/
	private int heroId;
	
	/**英雄升星前星级*/
	private int oldStar;
	
	/**英雄升星后星级*/
	private int newStar;

	public HeroUpStarEvent(){ super(null);}
	public HeroUpStarEvent(String playerId, int heroId, int oldStar, int newStar) {
		super(playerId);
		this.heroId = heroId;
		this.oldStar = oldStar;
		this.newStar = newStar;
	}

	public int getHeroId() {
		return heroId;
	}

	public int getOldStar() {
		return oldStar;
	}

	public int getNewStar() {
		return newStar;
	}

}

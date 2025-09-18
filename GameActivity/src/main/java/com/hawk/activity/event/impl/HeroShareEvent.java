package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 英雄分享
 * @author Jesse
 *
 */
public class HeroShareEvent extends ActivityEvent {
	
	private int heroId;

	public HeroShareEvent(){ super(null);}
	public HeroShareEvent(String playerId, int heroId) {
		super(playerId);
		this.heroId = heroId;
	}

	public int getHeroId() {
		return heroId;
	}

}

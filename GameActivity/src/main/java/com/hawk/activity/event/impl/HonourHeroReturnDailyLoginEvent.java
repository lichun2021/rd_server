package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 欢乐限购（红包）充值积分
 * 
 * @author lating
 *
 */
public class HonourHeroReturnDailyLoginEvent extends ActivityEvent {

	private int score;
	public HonourHeroReturnDailyLoginEvent(){ super(null);}
	public HonourHeroReturnDailyLoginEvent(String playerId, int score) {
		super(playerId);
		this.score = score;
	}
	
	public int getScore() {
		return score;
	}

}

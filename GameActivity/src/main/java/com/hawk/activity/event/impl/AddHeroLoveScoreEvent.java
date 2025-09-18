package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class AddHeroLoveScoreEvent extends ActivityEvent {
	
	/**
	 * 积分
	 */
	private int score;
	
	public AddHeroLoveScoreEvent(){ super(null);}
	public AddHeroLoveScoreEvent(String playerId, int score) {
		super(playerId);
		this.score = score;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}	
}

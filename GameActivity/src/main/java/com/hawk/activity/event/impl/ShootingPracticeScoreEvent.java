package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 八日庆典2增加积分事件
 * @author Jesse
 *
 */
public class ShootingPracticeScoreEvent extends ActivityEvent {

	private int score;
	
	public ShootingPracticeScoreEvent(){ super(null);}
	public ShootingPracticeScoreEvent(String playerId, int score) {
		super(playerId);
		this.score = score;
	}
	
	public int getScore() {
		return score;
	}

}

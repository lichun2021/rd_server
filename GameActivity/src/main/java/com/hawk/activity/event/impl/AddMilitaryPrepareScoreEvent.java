package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 军事备战增加积分
 * @author Winder
 *
 */
public class AddMilitaryPrepareScoreEvent extends ActivityEvent{
	private int score;

	public AddMilitaryPrepareScoreEvent(){ super(null);}
	public AddMilitaryPrepareScoreEvent(String playerId, int score) {
		super(playerId);
		this.score = score;
	}

	public int getScore() {
		return score;
	}
	
}

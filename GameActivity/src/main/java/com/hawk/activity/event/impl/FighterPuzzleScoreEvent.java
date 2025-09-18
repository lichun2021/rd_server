package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 武者拼图积分变更事件
 * @author Jesse
 *
 */
public class FighterPuzzleScoreEvent extends ActivityEvent {

	private int score;
	
	public FighterPuzzleScoreEvent(){ super(null);}
	public FighterPuzzleScoreEvent(String playerId, int score) {
		super(playerId);
		this.score = score;
	}
	
	public int getScore() {
		return score;
	}

}

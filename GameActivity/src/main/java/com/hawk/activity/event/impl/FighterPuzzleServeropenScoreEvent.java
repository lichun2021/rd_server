package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 武者拼图积分变更事件
 * @author Jesse
 *
 */
public class FighterPuzzleServeropenScoreEvent extends ActivityEvent {

	private int score;
	
	public FighterPuzzleServeropenScoreEvent(){ super(null);}
	public FighterPuzzleServeropenScoreEvent(String playerId, int score) {
		super(playerId);
		this.score = score;
	}
	
	public int getScore() {
		return score;
	}

}

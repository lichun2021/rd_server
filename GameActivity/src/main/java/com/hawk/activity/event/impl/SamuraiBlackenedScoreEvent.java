package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 黑武士积分变更事件
 * @author jm
 *
 */
public class SamuraiBlackenedScoreEvent extends ActivityEvent {

	private int score;
	
	public SamuraiBlackenedScoreEvent(){ super(null);}
	public SamuraiBlackenedScoreEvent(String playerId, int score) {
		super(playerId);
		this.score = score;
	}
	
	public int getScore() {
		return score;
	}

}

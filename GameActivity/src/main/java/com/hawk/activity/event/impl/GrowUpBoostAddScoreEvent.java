package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 中部培养计划积分增加时间
 * @author che
 *
 */
public class GrowUpBoostAddScoreEvent extends ActivityEvent {

	private int score;
	
	public GrowUpBoostAddScoreEvent(){ super(null);}
	public GrowUpBoostAddScoreEvent(String playerId, int score) {
		super(playerId);
		this.score = score;
	}
	
	public int getScore() {
		return score;
	}

}

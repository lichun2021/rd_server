package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 团购活动购买积分
 */
public class GroupBuyScoreEvent extends ActivityEvent {
	
	/**购买积分*/
	private int score;

	public GroupBuyScoreEvent(){ super(null);}
	public GroupBuyScoreEvent(String playerId, int score) {
		super(playerId);
		this.score = score;
	}

	public int getScore() {
		return score;
	}

}

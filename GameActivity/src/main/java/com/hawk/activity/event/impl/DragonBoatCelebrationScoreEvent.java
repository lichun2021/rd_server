package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 端午联盟庆典，联盟积分事件
 * @author Jesse
 *
 */
public class DragonBoatCelebrationScoreEvent extends ActivityEvent {

	private long score;


	public DragonBoatCelebrationScoreEvent(){ super(null);}
	public DragonBoatCelebrationScoreEvent(String playerId, long score) {
		super(playerId);
		this.score = score;
	}

	public long getScore() {
		return score;
	}
}

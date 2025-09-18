package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 能源滚滚联盟积分事件
 * @author Jesse
 *
 */
public class EnergiesGuildScoreEvent extends ActivityEvent {

	private long score;


	public EnergiesGuildScoreEvent(){ super(null);}
	public EnergiesGuildScoreEvent(String playerId, long score) {
		super(playerId);
		this.score = score;
	}

	public long getScore() {
		return score;
	}
}

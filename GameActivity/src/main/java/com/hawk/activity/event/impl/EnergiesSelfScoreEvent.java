package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 能源滚滚个人积分事件
 * @author Jesse
 *
 */
public class EnergiesSelfScoreEvent extends ActivityEvent {

	private int score;

	public EnergiesSelfScoreEvent(){ super(null);}
	public EnergiesSelfScoreEvent(String playerId, int score) {
		super(playerId);
		this.score = score;
	}

	public int getScore() {
		return score;
	}

}

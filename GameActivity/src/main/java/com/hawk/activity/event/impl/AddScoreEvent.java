package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 八日庆典增加积分事件
 * @author PhilChen
 *
 */
public class AddScoreEvent extends ActivityEvent {

	private int score;
	
	public AddScoreEvent(){ super(null);}
	public AddScoreEvent(String playerId, int score) {
		super(playerId);
		this.score = score;
	}
	
	public int getScore() {
		return score;
	}

}

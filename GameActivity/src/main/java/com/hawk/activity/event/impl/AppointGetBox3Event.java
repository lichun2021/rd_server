package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 八日庆典增加积分事件
 * @author PhilChen
 *
 */
public class AppointGetBox3Event extends ActivityEvent {

	private int score;
	
	public AppointGetBox3Event(){ super(null);}
	public AppointGetBox3Event(String playerId, int score) {
		super(playerId);
		this.score = score;
	}
	
	public int getScore() {
		return score;
	}

}

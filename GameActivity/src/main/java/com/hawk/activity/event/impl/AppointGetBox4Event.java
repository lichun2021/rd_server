package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 八日庆典增加积分事件
 * @author PhilChen
 *
 */
public class AppointGetBox4Event extends ActivityEvent {

	private int score;
	
	public AppointGetBox4Event(){ super(null);}
	public AppointGetBox4Event(String playerId, int score) {
		super(playerId);
		this.score = score;
	}
	
	public int getScore() {
		return score;
	}

}

package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 欢乐限购（红包）充值积分
 * 
 * @author lating
 *
 */
public class HappyRedRechargeScoreEvent extends ActivityEvent {

	private int score;
	
	public HappyRedRechargeScoreEvent(){ super(null);}
	public HappyRedRechargeScoreEvent(String playerId, int score) {
		super(playerId);
		this.score = score;
	}
	
	public int getScore() {
		return score;
	}

}

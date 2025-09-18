package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 双十一拼图四点连线增加积分
 * @author hf
 *
 */
public class AddJigsawConnectScoreEvent extends ActivityEvent{
	private int score;

	public AddJigsawConnectScoreEvent(){ super(null);}
	public AddJigsawConnectScoreEvent(String playerId, int score) {
		super(playerId);
		this.score = score;
	}

	public int getScore() {
		return score;
	}
	
}

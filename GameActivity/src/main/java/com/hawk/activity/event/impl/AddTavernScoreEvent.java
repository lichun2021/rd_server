package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.SpaceMechaEvent;

/**
 * 增加酒馆积分事件
 * @author PhilChen
 *
 */
public class AddTavernScoreEvent extends ActivityEvent implements SpaceMechaEvent {

	private int score;
	
	private int totalScore;
	
	public AddTavernScoreEvent(){ super(null);}
	public AddTavernScoreEvent(String playerId, int score, int totalScore) {
		super(playerId);
		this.score = score;
		this.totalScore = totalScore;
	}
	
	public int getScore() {
		return score;
	}

	public int getTotalScore() {
		return totalScore;
	}

}

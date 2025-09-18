package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 星能探索积分变化事件
 * 
 * @author lating
 * 
 */
public class PlanetExploreScoreEvent extends ActivityEvent {

	private int totalScore;

	public PlanetExploreScoreEvent(){ super(null);}
	public PlanetExploreScoreEvent(String playerId, int totalScore) {
		super(playerId);
		this.totalScore = totalScore;
	}
	
	public int getTotalScore() {
		return totalScore;
	}
	
}

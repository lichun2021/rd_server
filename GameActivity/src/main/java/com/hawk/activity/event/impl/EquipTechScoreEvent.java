package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 抽装备积分事件
 */
public class EquipTechScoreEvent extends ActivityEvent implements OrderEvent {

	/** 积分 */
	private int score;


	public EquipTechScoreEvent(){ super(null);}
	public EquipTechScoreEvent(String playerId, int score) {
		super(playerId);
		this.score = score;
	}


	public int getScore() {
		return score;
	}


	public void setScore(int score) {
		this.score = score;
	}

}

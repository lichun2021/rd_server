package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 冠军试练事件
 * @author Jesse
 *
 */
public class CrScoreEvent extends ActivityEvent implements OrderEvent {
	private int score;

	public CrScoreEvent(){ super(null);}
	public CrScoreEvent(String playerId, int score) {
		super(playerId);
		this.score = score;
	}

	public final int getScore() {
		return score;
	}

}

package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 能量收集积分变更事件
 * @author lating
 *
 */
public class EnergyGatherScoreEvent extends ActivityEvent {

	private int score;
	
	public EnergyGatherScoreEvent(){ super(null);}
	public EnergyGatherScoreEvent(String playerId, int score) {
		super(playerId);
		this.score = score;
	}
	
	public int getScore() {
		return score;
	}

}

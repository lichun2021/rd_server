package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 士兵死伤
 * @author che
 *
 */
public class HotBloodWarScoreEvent extends ActivityEvent {

	private long enemyKillScore;
	
	private long selfHurtScore;
	
	private long totalScore;
	
	
	public HotBloodWarScoreEvent(){ super(null);}
	public HotBloodWarScoreEvent(String playerId,long enemyKillScore,long selfHurtScore,long total) {
		super(playerId);
		this.enemyKillScore = enemyKillScore;
		this.selfHurtScore = selfHurtScore;
		this.totalScore = total;
	}
	
	public long getEnemyKillScore() {
		return enemyKillScore;
	}
	
	
	public long getSelfHurtScore() {
		return selfHurtScore;
	}
	
	public long getTotalScore() {
		return totalScore;
	}
}

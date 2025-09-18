package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 航海远征结算
 * @author Jesse
 *
 */
public class CrossScoreEvent extends ActivityEvent implements OrderEvent {
	
	
	private boolean crossOut;
	private long selfScore;
	private long guildScore;
	
	public CrossScoreEvent(){ super(null);}
	public CrossScoreEvent(String playerId, boolean crossOut,long selfScore, long guildScore) {
		super(playerId,true);
		this.crossOut = crossOut;
		this.selfScore = selfScore;
		this.guildScore = guildScore;
	}

	public boolean isCrossOut() {
		return crossOut;
	}
	
	public void setCrossOut(boolean crossOut) {
		this.crossOut = crossOut;
	}
	
	public long getSelfScore() {
		return selfScore;
	}
	
	public void setSelfScore(long selfScore) {
		this.selfScore = selfScore;
	}
	
	public long getGuildScore() {
		return guildScore;
	}

	public void setGuildScore(long guildScore) {
		this.guildScore = guildScore;
	}
}

package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 
 * @author che
 *
 */
public class GuildDragonAttackScoreEvent extends ActivityEvent {

	
	private int score;

	public GuildDragonAttackScoreEvent(){ super(null);}
	public GuildDragonAttackScoreEvent(String playerId, int score) {
		super(playerId);
		this.score = score;
	}

	public int getScore() {
		return score;
	}

	

}

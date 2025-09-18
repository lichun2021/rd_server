package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 
 * @author che
 *
 */
public class GuildDragonAttackScoreMaxEvent extends ActivityEvent {

	
	private int score;

	public GuildDragonAttackScoreMaxEvent(){ super(null);}
	public GuildDragonAttackScoreMaxEvent(String playerId, int score) {
		super(playerId);
		this.score = score;
	}

	public int getScore() {
		return score;
	}

	

}

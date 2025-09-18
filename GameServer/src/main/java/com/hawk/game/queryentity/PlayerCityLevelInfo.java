package com.hawk.game.queryentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class PlayerCityLevelInfo {
	@Id
	@Column(name = "playerId")
	private String playerId;

	@Column(name = "level")
	private int level;

	public String getPlayerId() {
		return playerId;
	}
	
	public int getLevel() {
		return level;
	}

}
package com.hawk.game.queryentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 名字信息, 主要用来搜索
 *
 * @author hawk
 *
 */
@Entity
public class NameIdInfo {
	@Id
	@Column(name = "playerId")
	protected String playerId;

	@Column(name = "playerName")
	protected String playerName = null;

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public String getPlayerName() {
		return playerName;
	}
}

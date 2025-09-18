package com.hawk.game.queryentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 城市保护罩信息
 *
 * @author lating
 *
 */
@Entity
public class CityShieldInfo {
	@Id
	@Column(name = "playerId")
	protected String playerId;

	@Column(name = "endTime")
	protected long endTime = 0;
	
	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	
}

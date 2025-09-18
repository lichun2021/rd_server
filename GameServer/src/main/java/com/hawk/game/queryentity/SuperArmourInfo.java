package com.hawk.game.queryentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 神器信息
 * @author golden
 *
 */
@Entity
public class SuperArmourInfo {
	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "playerId")
	protected String playerId;
	
	@Column(name = "endTime")
	protected long endTime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

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

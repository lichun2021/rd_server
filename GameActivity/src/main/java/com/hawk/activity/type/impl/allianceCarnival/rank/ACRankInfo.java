package com.hawk.activity.type.impl.allianceCarnival.rank;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ACRankInfo {

	@Id
	@Column(name = "playerId")
	public String playerId;
	
	@Column(name = "initGuildId")
	public String initGuildId;
	
	@Column(name = "finishTimes")
	public int finishTimes;
	
	@Column(name = "exp")
	public int exp;

	public String getInitGuildId() {
		return initGuildId;
	}

	public void setInitGuildId(String initGuildId) {
		this.initGuildId = initGuildId;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getFinishTimes() {
		return finishTimes;
	}

	public void setFinishTimes(int finishTimes) {
		this.finishTimes = finishTimes;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}
}

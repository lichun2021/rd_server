package com.hawk.activity.type.impl.redEnvelope.history;

import java.util.List;

public class PlayerRedEnvelopeHistory {
	
	private String playerId;
	
	private int stageId;
	
	private long time;
	
	private List<String> rewards;

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public int getStageId() {
		return stageId;
	}

	public void setStageId(int stageId) {
		this.stageId = stageId;
	}

	public List<String> getRewards() {
		return rewards;
	}

	public void setRewards(List<String> rewards) {
		this.rewards = rewards;
	}
}

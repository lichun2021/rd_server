package com.hawk.game.warcollege;

import java.util.HashMap;
import java.util.Map;

public class LMJYExtraParam {
	private int instanceId;
	private int battleId;
	private int teamId;
	private String leaderId;
	private String members;
	private Map<String, String> playerWinReward = new HashMap<>();

	public void putWinReward(String playerId, String reward) {
		playerWinReward.put(playerId, reward);
	}

	public int getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(int instanceId) {
		this.instanceId = instanceId;
	}

	public int getBattleId() {
		return battleId;
	}

	public void setBattleId(int battleId) {
		this.battleId = battleId;
	}

	public int getTeamId() {
		return teamId;
	}

	public void setTeamId(int teamId) {
		this.teamId = teamId;
	}

	public String getLeaderId() {
		return leaderId;
	}

	public void setLeaderId(String leaderId) {
		this.leaderId = leaderId;
	}

	public String getMembers() {
		return members;
	}

	public void setMembers(String members) {
		this.members = members;
	}

	public String getWinAward(String playerId) {
		return playerWinReward.getOrDefault(playerId, "");
	}

}

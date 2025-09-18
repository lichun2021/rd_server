package com.hawk.game.service.tiberium.logunit;

/**
 * 泰伯利亚玩家个人积分奖励发奖日志信息元
 * @author z
 *
 */
public class TWSelfRewardLogUnit {
	private String playerId;
	
	private String guildId;
	
	private String serverId;
	
	private long selfScore;
	
	private int season;
	
	private int termId;
	
	private int rewardId;
	
	private boolean isLeagua;

	public TWSelfRewardLogUnit(String playerId, String guildId, String serverId, long selfScore, int season, int termId, int rewardId, boolean isLeagua) {
		super();
		this.playerId = playerId;
		this.guildId = guildId;
		this.serverId = serverId;
		this.selfScore = selfScore;
		this.season = season;
		this.termId = termId;
		this.rewardId = rewardId;
		this.isLeagua = isLeagua;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public long getSelfScore() {
		return selfScore;
	}

	public void setSelfScore(long selfScore) {
		this.selfScore = selfScore;
	}

	public int getSeason() {
		return season;
	}

	public void setSeason(int season) {
		this.season = season;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public int getRewardId() {
		return rewardId;
	}

	public void setRewardId(int rewardId) {
		this.rewardId = rewardId;
	}

	public boolean isLeagua() {
		return isLeagua;
	}

	public void setLeagua(boolean isLeagua) {
		this.isLeagua = isLeagua;
	}
	
}

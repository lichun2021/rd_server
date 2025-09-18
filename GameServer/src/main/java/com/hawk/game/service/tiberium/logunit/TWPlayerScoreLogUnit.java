package com.hawk.game.service.tiberium.logunit;

/**
 * 泰伯利亚玩家战场积分日志信息元
 * @author z
 *
 */
public class TWPlayerScoreLogUnit {
	private String playerId;
	
	private int termId;
	
	private String guildId;
	
	private long score;

	public TWPlayerScoreLogUnit(String playerId, int termId, String guildId, long score) {
		super();
		this.playerId = playerId;
		this.termId = termId;
		this.guildId = guildId;
		this.score = score;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public long getScore() {
		return score;
	}

	public void setScore(long score) {
		this.score = score;
	}
	
}

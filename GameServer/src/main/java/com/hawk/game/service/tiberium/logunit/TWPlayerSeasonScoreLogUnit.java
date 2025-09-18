package com.hawk.game.service.tiberium.logunit;

/**
 * 泰伯联赛参与玩家积分战力数据日志信息元
 * 
 * @author z
 *
 */
public class TWPlayerSeasonScoreLogUnit {

	private String playerId;

	private String roomId;

	private String roomServer;

	private String guildId;

	private String guildName;

	private long score;

	private boolean isWin;

	public TWPlayerSeasonScoreLogUnit(String playerId, String roomId, String roomServer, String guildId, String guildName, long score, boolean isWin) {
		super();
		this.playerId = playerId;
		this.roomId = roomId;
		this.roomServer = roomServer;
		this.guildId = guildId;
		this.guildName = guildName;
		this.score = score;
		this.isWin = isWin;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public String getRoomServer() {
		return roomServer;
	}

	public void setRoomServer(String roomServer) {
		this.roomServer = roomServer;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public String getGuildName() {
		return guildName;
	}

	public void setGuildName(String guildName) {
		this.guildName = guildName;
	}

	public long getScore() {
		return score;
	}

	public void setScore(long score) {
		this.score = score;
	}

	public boolean isWin() {
		return isWin;
	}

	public void setWin(boolean isWin) {
		this.isWin = isWin;
	}

}

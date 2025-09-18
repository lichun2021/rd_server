package com.hawk.game.service.tiberium.logunit;

/**
 * 泰伯利亚玩家战场积分日志信息元
 * @author z
 *
 */
public class TWGuildScoreLogUnit {
	private String guildId;
	
	private String guildName;
	
	private int termId;
	
	private String serverId;
	
	private String roomId;
	
	private String roomServer;
	
	private long score;
	
	private int memberCnt;
	
	private long totalPower;
	
	private boolean isWin;

	public TWGuildScoreLogUnit(String guildId, String guildName, int termId, String serverId, String roomId, String roomServer, long score, int memberCnt, long totalPower,
			boolean isWin) {
		super();
		this.guildId = guildId;
		this.guildName = guildName;
		this.termId = termId;
		this.serverId = serverId;
		this.roomId = roomId;
		this.roomServer = roomServer;
		this.score = score;
		this.memberCnt = memberCnt;
		this.totalPower = totalPower;
		this.isWin = isWin;
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

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
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

	public long getScore() {
		return score;
	}

	public void setScore(long score) {
		this.score = score;
	}

	public int getMemberCnt() {
		return memberCnt;
	}

	public void setMemberCnt(int memberCnt) {
		this.memberCnt = memberCnt;
	}

	public long getTotalPower() {
		return totalPower;
	}

	public void setTotalPower(long totalPower) {
		this.totalPower = totalPower;
	}

	public boolean isWin() {
		return isWin;
	}

	public void setWin(boolean isWin) {
		this.isWin = isWin;
	}
	
}

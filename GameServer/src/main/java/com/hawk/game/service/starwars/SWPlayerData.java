package com.hawk.game.service.starwars;

/**
 *  泰伯利亚之战玩家信息
 * @author Jesse
 */
public class SWPlayerData {
	public String id;
	
	public String serverId;

	public String guildId;

	public int guildAuth;

	public int guildOfficer;

	/** 进入战场时间 */
	public long enterTime;

	/** 退出战场时间 */
	public long quitTime;

	public long score;

	public long killPower;

	public long deadPower;

	public boolean isAwarded;

	/** 是否中途退出 */
	public boolean isMidwayQuit;

	public final String getId() {
		return id;
	}

	public final void setId(String id) {
		this.id = id;
	}

	public final String getGuildId() {
		return guildId;
	}

	public final void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public final int getGuildAuth() {
		return guildAuth;
	}

	public final void setGuildAuth(int guildAuth) {
		this.guildAuth = guildAuth;
	}

	public final int getGuildOfficer() {
		return guildOfficer;
	}

	public final void setGuildOfficer(int guildOfficer) {
		this.guildOfficer = guildOfficer;
	}

	public final long getEnterTime() {
		return enterTime;
	}

	public final void setEnterTime(long enterTime) {
		this.enterTime = enterTime;
	}

	public long getQuitTime() {
		return quitTime;
	}

	public void setQuitTime(long quitTime) {
		this.quitTime = quitTime;
	}

	public final long getScore() {
		return score;
	}

	public final void setScore(long score) {
		this.score = score;
	}

	public boolean isAwarded() {
		return isAwarded;
	}

	public void setAwarded(boolean isAwarded) {
		this.isAwarded = isAwarded;
	}

	public boolean isMidwayQuit() {
		return isMidwayQuit;
	}

	public void setMidwayQuit(boolean isMidwayQuit) {
		this.isMidwayQuit = isMidwayQuit;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public long getKillPower() {
		return killPower;
	}

	public void setKillPower(long killPower) {
		this.killPower = killPower;
	}

	public long getDeadPower() {
		return deadPower;
	}

	public void setDeadPower(long deadPower) {
		this.deadPower = deadPower;
	}
}

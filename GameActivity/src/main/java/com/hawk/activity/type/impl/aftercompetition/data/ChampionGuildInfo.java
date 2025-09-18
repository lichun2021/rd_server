package com.hawk.activity.type.impl.aftercompetition.data;

/**
 * 冠军联盟信息
 * 
 * @author lating
 *
 */
public class ChampionGuildInfo {

	private String guildId;
	private String guildName;
	private String leaderId;
	private String leaderName;
	private String serverId;
	private int rank;
	private int leaderIcon;
	private String leaderPfIcon;
	
	public ChampionGuildInfo() {
	}
	
	public ChampionGuildInfo(String guildId, String guildName, String leaderId, String leaderName, int icon, String pfIcon, String serverId, int rank) {
		this.guildId = guildId;
		this.guildName = guildName;
		this.leaderId = leaderId;
		this.leaderName = leaderName;
		this.leaderIcon = icon;
		this.leaderPfIcon = pfIcon;
		this.serverId = serverId;
		this.rank = rank;
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
	
	public String getServerId() {
		return serverId;
	}
	
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getLeaderId() {
		return leaderId;
	}

	public void setLeaderId(String leaderId) {
		this.leaderId = leaderId;
	}

	public String getLeaderName() {
		return leaderName;
	}

	public void setLeaderName(String leaderName) {
		this.leaderName = leaderName;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getLeaderIcon() {
		return leaderIcon;
	}

	public void setLeaderIcon(int leaderIcon) {
		this.leaderIcon = leaderIcon;
	}

	public String getLeaderPfIcon() {
		return leaderPfIcon;
	}

	public void setLeaderPfIcon(String leaderPfIcon) {
		this.leaderPfIcon = leaderPfIcon;
	}
	
}

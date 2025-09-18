package com.hawk.game.module.lianmengfgyl.battleroom;

import com.google.common.base.MoreObjects;

public class FGYLExtraParam {
	private int difficult;
	private boolean isLeaguaWar = false;
	private int season = 0;
	private String battleId = "";
	private String campAGuild = "";
	private String campAGuildName = "";
	private String campAGuildTag = "";
	private String campAServerId = "";
	private String teamAName = "";
	private int campAguildFlag;

	private String campBGuild = "";
	private String campBGuildName = "";
	private String campBGuildTag = "";
	private String campBServerId = "";
	private int campBguildFlag;
	private String teamBName = "";

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("isLeaguaWar", isLeaguaWar)
				.add("battleId", battleId)
				.add("season", season)
				.add("campAGuild", campAGuild)
				.add("campAGuildName", campAGuildName)
				.add("campAGuildTag", campAGuildTag)
				.add("campAServerId", campAServerId)
				.add("campAguildFlag", campAguildFlag)
				.add("campBGuild", campBGuild)
				.add("campBGuildName", campBGuildName)
				.add("campBGuildTag", campBGuildTag)
				.add("campBServerId", campBServerId)
				.add("campBguildFlag", campBguildFlag)
				.toString();
	}

	public int getDifficult() {
		return difficult;
	}

	public void setDifficult(int difficult) {
		this.difficult = difficult;
	}

	public boolean isLeaguaWar() {
		return isLeaguaWar;
	}

	public void setLeaguaWar(boolean isLeaguaWar) {
		this.isLeaguaWar = isLeaguaWar;
	}

	public String getCampAGuild() {
		return campAGuild;
	}

	public void setCampAGuild(String campAGuild) {
		this.campAGuild = campAGuild;
	}

	public String getCampAGuildName() {
		return campAGuildName;
	}

	public void setCampAGuildName(String campAGuildName) {
		this.campAGuildName = campAGuildName;
	}

	public String getCampAGuildTag() {
		return campAGuildTag;
	}

	public void setCampAGuildTag(String campAGuildTag) {
		this.campAGuildTag = campAGuildTag;
	}

	public String getCampAServerId() {
		return campAServerId;
	}

	public void setCampAServerId(String campAServerId) {
		this.campAServerId = campAServerId;
	}

	public int getCampAguildFlag() {
		return campAguildFlag;
	}

	public void setCampAguildFlag(int campAguildFlag) {
		this.campAguildFlag = campAguildFlag;
	}

	public String getCampBGuild() {
		return campBGuild;
	}

	public void setCampBGuild(String campBGuild) {
		this.campBGuild = campBGuild;
	}

	public String getCampBGuildName() {
		return campBGuildName;
	}

	public void setCampBGuildName(String campBGuildName) {
		this.campBGuildName = campBGuildName;
	}

	public String getCampBGuildTag() {
		return campBGuildTag;
	}

	public void setCampBGuildTag(String campBGuildTag) {
		this.campBGuildTag = campBGuildTag;
	}

	public String getCampBServerId() {
		return campBServerId;
	}

	public void setCampBServerId(String campBServerId) {
		this.campBServerId = campBServerId;
	}

	public int getCampBguildFlag() {
		return campBguildFlag;
	}

	public void setCampBguildFlag(int campBguildFlag) {
		this.campBguildFlag = campBguildFlag;
	}

	public int getSeason() {
		return season;
	}

	public void setSeason(int season) {
		this.season = season;
	}

	public String getBattleId() {
		return battleId;
	}

	public void setBattleId(String battleId) {
		this.battleId = battleId;
	}

	public String getTeamAName() {
		return teamAName;
	}

	public void setTeamAName(String teamAName) {
		this.teamAName = teamAName;
	}

	public String getTeamBName() {
		return teamBName;
	}

	public void setTeamBName(String teamBName) {
		this.teamBName = teamBName;
	}

}

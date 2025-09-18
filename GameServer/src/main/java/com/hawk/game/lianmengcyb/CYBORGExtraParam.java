package com.hawk.game.lianmengcyb;

import com.google.common.base.MoreObjects;

public class CYBORGExtraParam {
	private boolean isLeaguaWar = false;
	private int season = 0;
	private String battleId="";
	
	private String campAGuild = "";
	private String campAGuildName = "";
	private String campAGuildTag = "";
	private String campAServerId = "";
	private int campAguildFlag;
	private String campATeamName = "";
	private long campATeamPower;

	private String campBGuild = "";
	private String campBGuildName = "";
	private String campBGuildTag = "";
	private String campBServerId = "";
	private int campBguildFlag;
	private String campBTeamName = "";
	private long campBTeamPower;

	private String campCGuild = "";
	private String campCGuildName = "";
	private String campCGuildTag = "";
	private String campCServerId = "";
	private int campCguildFlag;
	private String campCTeamName = "";
	private long campCTeamPower;

	private String campDGuild = "";
	private String campDGuildName = "";
	private String campDGuildTag = "";
	private String campDServerId = "";
	private int campDguildFlag;
	private String campDTeamName = "";
	private long campDTeamPower;

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

				.add("campCGuild", campCGuild)
				.add("campCGuildName", campCGuildName)
				.add("campCGuildTag", campCGuildTag)
				.add("campCServerId", campCServerId)
				.add("campCguildFlag", campCguildFlag)
				
				.add("campDGuild", campDGuild)
				.add("campDGuildName", campDGuildName)
				.add("campDGuildTag", campDGuildTag)
				.add("campDServerId", campDServerId)
				.add("campDguildFlag", campDguildFlag)
				
				.toString();
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

	public String getCampCGuild() {
		return campCGuild;
	}

	public void setCampCGuild(String campCGuild) {
		this.campCGuild = campCGuild;
	}

	public String getCampCGuildName() {
		return campCGuildName;
	}

	public void setCampCGuildName(String campCGuildName) {
		this.campCGuildName = campCGuildName;
	}

	public String getCampCGuildTag() {
		return campCGuildTag;
	}

	public void setCampCGuildTag(String campCGuildTag) {
		this.campCGuildTag = campCGuildTag;
	}

	public String getCampCServerId() {
		return campCServerId;
	}

	public void setCampCServerId(String campCServerId) {
		this.campCServerId = campCServerId;
	}

	public int getCampCguildFlag() {
		return campCguildFlag;
	}

	public void setCampCguildFlag(int campCguildFlag) {
		this.campCguildFlag = campCguildFlag;
	}

	public String getCampDGuild() {
		return campDGuild;
	}

	public void setCampDGuild(String campDGuild) {
		this.campDGuild = campDGuild;
	}

	public String getCampDGuildName() {
		return campDGuildName;
	}

	public void setCampDGuildName(String campDGuildName) {
		this.campDGuildName = campDGuildName;
	}

	public String getCampDGuildTag() {
		return campDGuildTag;
	}

	public void setCampDGuildTag(String campDGuildTag) {
		this.campDGuildTag = campDGuildTag;
	}

	public String getCampDServerId() {
		return campDServerId;
	}

	public void setCampDServerId(String campDServerId) {
		this.campDServerId = campDServerId;
	}

	public int getCampDguildFlag() {
		return campDguildFlag;
	}

	public void setCampDguildFlag(int campDguildFlag) {
		this.campDguildFlag = campDguildFlag;
	}

	public String getCampATeamName() {
		return campATeamName;
	}

	public void setCampATeamName(String campATeamName) {
		this.campATeamName = campATeamName;
	}

	public long getCampATeamPower() {
		return campATeamPower;
	}

	public void setCampATeamPower(long campATeamPower) {
		this.campATeamPower = campATeamPower;
	}

	public String getCampBTeamName() {
		return campBTeamName;
	}

	public void setCampBTeamName(String campBTeamName) {
		this.campBTeamName = campBTeamName;
	}

	public long getCampBTeamPower() {
		return campBTeamPower;
	}

	public void setCampBTeamPower(long campBTeamPower) {
		this.campBTeamPower = campBTeamPower;
	}

	public String getCampCTeamName() {
		return campCTeamName;
	}

	public void setCampCTeamName(String campCTeamName) {
		this.campCTeamName = campCTeamName;
	}

	public long getCampCTeamPower() {
		return campCTeamPower;
	}

	public void setCampCTeamPower(long campCTeamPower) {
		this.campCTeamPower = campCTeamPower;
	}

	public String getCampDTeamName() {
		return campDTeamName;
	}

	public void setCampDTeamName(String campDTeamName) {
		this.campDTeamName = campDTeamName;
	}

	public long getCampDTeamPower() {
		return campDTeamPower;
	}

	public void setCampDTeamPower(long campDTeamPower) {
		this.campDTeamPower = campDTeamPower;
	}

}

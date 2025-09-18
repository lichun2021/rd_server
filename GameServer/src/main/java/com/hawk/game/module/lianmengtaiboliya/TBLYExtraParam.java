package com.hawk.game.module.lianmengtaiboliya;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.hawk.game.service.tiberium.TWPlayerData;

public class TBLYExtraParam {
	private boolean isLeaguaWar = false;
	private int season = 0;
	private String battleId="";
	private String campAGuild = "";
	private String campAGuildName = "";
	private String campAGuildTag = "";
	private String campAServerId = "";
	private int campAguildFlag;
	private List<TWPlayerData> campAPlayers = new ArrayList<>();

	private String campBGuild = "";
	private String campBGuildName = "";
	private String campBGuildTag = "";
	private String campBServerId = "";
	private int campBguildFlag;
	private List<TWPlayerData> campBPlayers = new ArrayList<>();

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

	public List<TWPlayerData> getCampAPlayers() {
		return campAPlayers;
	}

	public void setCampAPlayers(List<TWPlayerData> campAPlayers) {
		this.campAPlayers = campAPlayers;
	}

	public List<TWPlayerData> getCampBPlayers() {
		return campBPlayers;
	}

	public void setCampBPlayers(List<TWPlayerData> campBPlayers) {
		this.campBPlayers = campBPlayers;
	}

}

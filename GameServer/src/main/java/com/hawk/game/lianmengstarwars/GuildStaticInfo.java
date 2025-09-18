package com.hawk.game.lianmengstarwars;

import java.util.HashSet;
import java.util.Set;

public class GuildStaticInfo {
	String campAGuild = "";
	String campAGuildName = "";
	String campAGuildTag = "";
	String campAServerId = "";
	int campAguildFlag;
	long honorA = 0; // 当前积分
	int perMinA = 0; // 每分增加
	int buildCountA = 0; // 占领建筑
	int playerCountA = 0; // 战场中人数
	long centerControlA = 0; // SW_HEADQUARTERS 核心控制时间
	int buildControlHonorA = 0;
	long killHonorA = 0;
	String csServerId;
	int campAGuildWarCount;
	Set<String> playerIds = new HashSet<>();
	int towerCnt;
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

	public long getHonorA() {
		return honorA;
	}

	public void setHonorA(long honorA) {
		this.honorA = honorA;
	}

	public int getPerMinA() {
		return perMinA;
	}

	public void setPerMinA(int perMinA) {
		this.perMinA = perMinA;
	}

	public int getBuildCountA() {
		return buildCountA;
	}

	public void setBuildCountA(int buildCountA) {
		this.buildCountA = buildCountA;
	}

	public int getPlayerCountA() {
		return playerCountA;
	}

	public void setPlayerCountA(int playerCountA) {
		this.playerCountA = playerCountA;
	}

	public long getCenterControlA() {
		return centerControlA;
	}

	public void setCenterControlA(long centerControlA) {
		this.centerControlA = centerControlA;
	}

	public int getBuildControlHonorA() {
		return buildControlHonorA;
	}

	public void setBuildControlHonorA(int buildControlHonorA) {
		this.buildControlHonorA = buildControlHonorA;
	}

	public long getKillHonorA() {
		return killHonorA;
	}

	public void setKillHonorA(long killHonorA) {
		this.killHonorA = killHonorA;
	}

	public int getCampAGuildWarCount() {
		return campAGuildWarCount;
	}

	public void setCampAGuildWarCount(int campAGuildWarCount) {
		this.campAGuildWarCount = campAGuildWarCount;
	}

	public Set<String> getPlayerIds() {
		return playerIds;
	}

	public void setPlayerIds(Set<String> playerIds) {
		this.playerIds = playerIds;
	}

	public String getCsServerId() {
		return csServerId;
	}

	public void setCsServerId(String csServerId) {
		this.csServerId = csServerId;
	}

	public int getTowerCnt() {
		return towerCnt;
	}

	public void setTowerCnt(int towerCnt) {
		this.towerCnt = towerCnt;
	}

}
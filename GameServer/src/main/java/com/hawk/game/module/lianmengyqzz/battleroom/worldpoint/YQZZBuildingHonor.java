package com.hawk.game.module.lianmengyqzz.battleroom.worldpoint;

import com.hawk.game.protocol.YQZZ.PBYQZZBuildHonor;

public class YQZZBuildingHonor {

	private String guildId;
	private String serverId;
	private int buildId;
	private int x;
	private int y;

	private long controlTime; // 累计控制时长
	private double playerHonor; // 积分
	private double guildHonor; // 积分
	private double nationHonor; // 积分

	private double firstControlPlayerHonor; // 积分
	private double firstControlGuildHonor; // 积分
	private double firstControlNationHonor; // 积分

	public PBYQZZBuildHonor toPBObj() {
		PBYQZZBuildHonor.Builder builder = PBYQZZBuildHonor.newBuilder();
		builder.setServerId(serverId);
		builder.setGuildId(guildId);
		builder.setBuildId(buildId);
		builder.setX(x);
		builder.setY(y);
		builder.setPlayerHonor((int) (playerHonor + firstControlPlayerHonor));
		builder.setGuildHonor((int) (guildHonor + firstControlGuildHonor));
		builder.setNationHonor((int) (nationHonor + firstControlNationHonor));
		return builder.build();
	}

	public double getPlayerHonor() {
		return playerHonor;
	}

	public void setPlayerHonor(double playerHonor) {
		this.playerHonor = playerHonor;
	}

	public double getGuildHonor() {
		return guildHonor;
	}

	public void setGuildHonor(double guildHonor) {
		this.guildHonor = guildHonor;
	}

	public double getNationHonor() {
		return nationHonor;
	}

	public void setNationHonor(double nationHonor) {
		this.nationHonor = nationHonor;
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

	public int getBuildId() {
		return buildId;
	}

	public void setBuildId(int buildId) {
		this.buildId = buildId;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public long getControlTime() {
		return controlTime;
	}

	public void setControlTime(long controlTime) {
		this.controlTime = controlTime;
	}

	public double getFirstControlPlayerHonor() {
		return firstControlPlayerHonor;
	}

	public void setFirstControlPlayerHonor(double firstControlPlayerHonor) {
		this.firstControlPlayerHonor = firstControlPlayerHonor;
	}

	public double getFirstControlGuildHonor() {
		return firstControlGuildHonor;
	}

	public void setFirstControlGuildHonor(double firstControlGuildHonor) {
		this.firstControlGuildHonor = firstControlGuildHonor;
	}

	public double getFirstControlNationHonor() {
		return firstControlNationHonor;
	}

	public void setFirstControlNationHonor(double firstControlNationHonor) {
		this.firstControlNationHonor = firstControlNationHonor;
	}

}

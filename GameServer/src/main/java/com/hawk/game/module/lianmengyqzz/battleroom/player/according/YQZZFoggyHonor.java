package com.hawk.game.module.lianmengyqzz.battleroom.player.according;

import com.hawk.game.protocol.YQZZ.PBYQZZFoggyHonor;

public class YQZZFoggyHonor {
	private int foggyFortressId ;
	private int killCount;
	private int joinKillCount;
	private double playerHonor; // 积分
	private double guildHonor; // 积分
	private double nationHonor; // 积分

	public PBYQZZFoggyHonor toPBObj() {
		PBYQZZFoggyHonor.Builder builder = PBYQZZFoggyHonor.newBuilder();
		builder.setFoggyFortressId(foggyFortressId);
		builder.setKillCount(killCount);
		builder.setPlayerHonor((int) playerHonor);
		builder.setGuildHonor((int) guildHonor);
		builder.setNationHonor((int) nationHonor);
		return builder.build();
	}

	public int getFoggyFortressId() {
		return foggyFortressId;
	}

	public void setFoggyFortressId(int foggyFortressId) {
		this.foggyFortressId = foggyFortressId;
	}

	public int getKillCount() {
		return killCount;
	}

	public void setKillCount(int killCount) {
		this.killCount = killCount;
	}

	public int getJoinKillCount() {
		return joinKillCount;
	}

	public void setJoinKillCount(int joinKillCount) {
		this.joinKillCount = joinKillCount;
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

}

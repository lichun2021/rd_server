package com.hawk.game.module.lianmengyqzz.battleroom.player.according;

import com.hawk.game.protocol.YQZZ.PBYQZZMonsterHonor;

public class YQZZMonsterHonor {
	private int monsterId;
	private int killCount;
	private double playerHonor; // 积分
	private double guildHonor; // 积分
	private double nationHonor; // 积分

	public PBYQZZMonsterHonor toPBObj() {
		PBYQZZMonsterHonor.Builder builder = PBYQZZMonsterHonor.newBuilder();
		builder.setMonsterId(monsterId);
		builder.setKillCount(killCount);
		builder.setPlayerHonor((int) playerHonor);
		builder.setGuildHonor((int) guildHonor);
		builder.setNationHonor((int) nationHonor);
		return builder.build();
	}

	public int getMonsterId() {
		return monsterId;
	}

	public void setMonsterId(int monsterId) {
		this.monsterId = monsterId;
	}

	public int getKillCount() {
		return killCount;
	}

	public void setKillCount(int killCount) {
		this.killCount = killCount;
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

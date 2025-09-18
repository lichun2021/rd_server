package com.hawk.game.module.lianmengyqzz.battleroom.player.according;

import com.hawk.game.protocol.YQZZ.PBYQZZPylonHonor;

public class YQZZPylonHonor {
	private int pylonId;
	private int pylonMarchCount; // 出征数
	private int pylonCount;
	private double playerHonor; // 积分
	private double guildHonor; // 积分
	private double nationHonor; // 积分

	public PBYQZZPylonHonor toPBObj() {
		PBYQZZPylonHonor.Builder builder = PBYQZZPylonHonor.newBuilder();
		builder.setPylonId(pylonId);
		builder.setPylonCount(pylonId);
		builder.setPlayerHonor((int) playerHonor);
		builder.setGuildHonor((int) guildHonor);
		builder.setNationHonor((int) nationHonor);
		return builder.build();
	}

	public int getPylonId() {
		return pylonId;
	}

	public void setPylonId(int pylonId) {
		this.pylonId = pylonId;
	}

	public int getPylonCount() {
		return pylonCount;
	}

	public void setPylonCount(int pylonCount) {
		this.pylonCount = pylonCount;
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

	public int getPylonMarchCount() {
		return pylonMarchCount;
	}

	public void setPylonMarchCount(int pylonMarchCount) {
		this.pylonMarchCount = pylonMarchCount;
	}

}

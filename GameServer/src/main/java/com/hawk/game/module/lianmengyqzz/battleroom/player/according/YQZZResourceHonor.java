package com.hawk.game.module.lianmengyqzz.battleroom.player.according;

import com.hawk.game.protocol.YQZZ.PBYQZZResHonor;

public class YQZZResourceHonor {
	private int resourceId;
	private int resourceCount;
	private double playerHonor; // 积分
	private double guildHonor; // 积分
	private double nationHonor; // 积分

	public PBYQZZResHonor toPBObj() {
		PBYQZZResHonor.Builder builder = PBYQZZResHonor.newBuilder();
		builder.setResourceId(resourceId);
		builder.setResourceCount(resourceCount);
		builder.setPlayerHonor((int) playerHonor);
		builder.setGuildHonor((int) guildHonor);
		builder.setNationHonor((int) nationHonor);
		return builder.build();
	}

	public int getResourceId() {
		return resourceId;
	}

	public void setResourceId(int resourceId) {
		this.resourceId = resourceId;
	}

	public int getResourceCount() {
		return resourceCount;
	}

	public void setResourceCount(int resourceCount) {
		this.resourceCount = resourceCount;
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

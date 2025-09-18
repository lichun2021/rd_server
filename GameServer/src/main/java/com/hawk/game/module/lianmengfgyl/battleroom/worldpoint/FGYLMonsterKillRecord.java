package com.hawk.game.module.lianmengfgyl.battleroom.worldpoint;

import org.hawk.os.HawkTime;

import com.hawk.game.protocol.World.PBFGYLMonsterKillRank;

public class FGYLMonsterKillRecord {
	private String playerId;
	private String name;
	private int kill;
	private long lastUpdate;

	public void addKill(int count) {
		this.kill += count;
		lastUpdate = HawkTime.getMillisecond();
	}

	public PBFGYLMonsterKillRank toPBObj() {
		PBFGYLMonsterKillRank.Builder builder = PBFGYLMonsterKillRank.newBuilder();
		builder.setName(name);
		builder.setKill(kill);
		return builder.build();
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getKill() {
		return kill;
	}

	public void setKill(int kill) {
		this.kill = kill;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

}

package com.hawk.game.nation.wearhouse;

import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.National.NationalDiamondRecordPB;

public class NationalDiamondRecord {
	private long time;
	private String playerId;
	private String playerName;
	private long count;
	private int type;
	private int build;
	
	public NationalDiamondRecord() {}
	
	public static NationalDiamondRecord valueOf(int type, long time, String playerId, String playerName, long count, int build) {
		NationalDiamondRecord record = new NationalDiamondRecord();
		record.setType(type);
		record.setTime(time);
		record.setPlayerId(playerId);
		record.setPlayerName(playerName);
		record.setCount(count);
		record.setBuild(build);
		return record;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getBuild() {
		return build;
	}

	public void setBuild(int build) {
		this.build = build;
	}

	public NationalDiamondRecordPB.Builder toBuilder() {
		NationalDiamondRecordPB.Builder builder = NationalDiamondRecordPB.newBuilder();
		builder.setTime(time);
		builder.setPlayerId(playerId);
		builder.setPlayerName(playerName);
		builder.setItemId(PlayerAttr.DIAMOND_VALUE);
		builder.setCount(count);
		builder.setBuild(build);
		builder.setType(type);
		return builder;
	}
	
}

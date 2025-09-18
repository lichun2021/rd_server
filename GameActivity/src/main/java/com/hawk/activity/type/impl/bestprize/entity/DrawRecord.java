package com.hawk.activity.type.impl.bestprize.entity;

import org.hawk.os.HawkOSOperator;
import com.hawk.game.protocol.Activity.PoolDrawRecordPB;

/**
 * 抽奖记录
 */
public class DrawRecord {
	/**
	 * 大奖池id
	 */
	private int bigPool;
	/**
	 * 小奖池id
	 */
	private int poolId;
	/**
	 * 奖励类型：0-最终奖，1-A奖
	 */
	private int type;
	/**
	 * 抽奖玩家所在区服id
	 */
	private String serverId;
	/**
	 * 抽奖玩家名字
	 */
	private String playerName;
	/**
	 * 抽奖时间
	 */
	private long time;
	
	public DrawRecord() {}
	
	public DrawRecord(int bigPool, int poolId, int type, String serverId, String playerName, long time) {
		this.bigPool = bigPool;
		this.poolId = poolId;
		this.type = type;
		this.serverId = serverId;
		this.playerName = playerName;
		this.time = time;
	}

	public int getBigPool() {
		return bigPool;
	}

	public void setBigPool(int bigPool) {
		this.bigPool = bigPool;
	}

	public int getPoolId() {
		return poolId;
	}

	public void setPoolId(int poolId) {
		this.poolId = poolId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public PoolDrawRecordPB.Builder toBuilder() {
		PoolDrawRecordPB.Builder builder = PoolDrawRecordPB.newBuilder();
		builder.setBigPoolId(bigPool);
		builder.setSmallPoolId(poolId);
		builder.setServerId(serverId);
		builder.setPlayerName(playerName);
		builder.setType(type);
		builder.setTime(time);
		return builder;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(bigPool).append("|").append(poolId).append("|").append(serverId).append("|").append(playerName).append("|").append(type).append("|").append(time);
		return sb.toString();
	}
	
	public static DrawRecord toObject(String str) {
		if (HawkOSOperator.isEmptyString(str)) {
			return null;
		}
		
		String[] arr = str.split("\\|");
		if (arr.length < 6) {
			return null;
		}
		
		DrawRecord record = new DrawRecord();
		record.setBigPool(Integer.parseInt(arr[0]));
		record.setPoolId(Integer.parseInt(arr[1]));
		record.setServerId(arr[2]);
		record.setPlayerName(arr[3]);
		record.setType(Integer.parseInt(arr[4]));
		record.setTime(Long.parseLong(arr[5]));
		return record;
	}
}

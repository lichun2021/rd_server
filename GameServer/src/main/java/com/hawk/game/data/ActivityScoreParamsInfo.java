package com.hawk.game.data;

import org.hawk.os.HawkOSOperator;

/**
 * 活动积分处理参数信息
 * 
 * @author lating
 *
 */
public class ActivityScoreParamsInfo {
	/**
	 * 唯一标识
	 */
	private String uuid;
	/**
	 * 角色ID：如果是针对具体角色处理的，需要此信息
	 */
	private String playerId;
	/**
	 * 区服ID：如果是针对具体区服处理的，需要此信息
	 */
	private String serverId;
	/**
	 * 需要添加的积分数
	 */
	private int addScore;
	/**
	 * 截至时间
	 */
	private long endTime;
	
	public ActivityScoreParamsInfo() {
	}
	
	public ActivityScoreParamsInfo(String playerId, String serverId, int addScore, long endTime) {
		this.uuid = HawkOSOperator.randomUUID();
		this.playerId = playerId;
		this.serverId = serverId;
		this.addScore = addScore;
		this.endTime = endTime;
	}
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public int getAddScore() {
		return addScore;
	}

	public void setAddScore(int addScore) {
		this.addScore = addScore;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

}

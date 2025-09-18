package com.hawk.game.entity;

/**
 * 守护的邀请信息
 * @author jm
 *
 */
public class PlayerGuardInviteEntity {
	/**
	 * 邀请的玩家
	 */
	private String playerId;
	
	/**
	 * 被邀请的玩家
	 */
	private String targetPlayerId;
	
	/**
	 * 创建的时间.
	 */
	private int endTime;

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getTargetPlayerId() {
		return targetPlayerId;
	}

	public void setTargetPlayerId(String targetPlayerId) {
		this.targetPlayerId = targetPlayerId;
	}

	public int getEndTime() {
		return endTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}
}

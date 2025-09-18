package com.hawk.game.data;

import java.util.HashMap;
import java.util.Map;

/**
 * 新兵救援信息
 * 
 * @author lating
 *
 */
public class ProtectSoldierInfo {
	/**
	 * 玩家角色ID
	 */
	private String playerId;
	/**
	 * 领取士兵总量
	 */
	private int receiveTotalCount;
	/**
	 * 当日日领取士兵数量
	 */
	private int receiveCountDay;
	/**
	 * 上一次领取士兵的时间
	 */
	private long lastReceiveTime;
    /**
     * 死兵数量
     */
	private Map<Integer, Integer> deadSoldier;
	
	public ProtectSoldierInfo() {
		deadSoldier = new HashMap<Integer, Integer>();
	}
	
	public ProtectSoldierInfo(String playerId) {
		this.playerId = playerId;
		deadSoldier = new HashMap<Integer, Integer>();
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getReceiveTotalCount() {
		return receiveTotalCount;
	}

	public void setReceiveTotalCount(int receiveTotalCount) {
		this.receiveTotalCount = receiveTotalCount;
	}

	public int getReceiveCountDay() {
		return receiveCountDay;
	}

	public void setReceiveCountDay(int receiveCountDay) {
		this.receiveCountDay = receiveCountDay;
	}

	public long getLastReceiveTime() {
		return lastReceiveTime;
	}

	public void setLastReceiveTime(long lastReceiveTime) {
		this.lastReceiveTime = lastReceiveTime;
	}

	public Map<Integer, Integer> getDeadSoldier() {
		return deadSoldier;
	}

	public void setDeadSoldier(Map<Integer, Integer> deadSoldier) {
		this.deadSoldier = deadSoldier;
	}

}

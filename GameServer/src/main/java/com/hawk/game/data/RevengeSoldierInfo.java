package com.hawk.game.data;

import java.util.HashMap;
import java.util.Map;

import org.hawk.uuid.HawkUUIDGenerator;

/**
 * 大R复仇死兵信息
 * 
 * @author lating
 *
 */
public class RevengeSoldierInfo {
	/**
	 * uuid
	 */
	private String uuid;
	/**
	 * 玩家角色ID
	 */
	private String playerId;
	/**
	 * 死兵总数量
	 */
	private int totalCount;
	/**
	 * 死兵时间
	 */
	private long soldierDeadTime;
    /**
     * 死兵数量
     */
	private Map<Integer, Integer> deadSoldier;
	
	public RevengeSoldierInfo() {
		deadSoldier = new HashMap<Integer, Integer>();
	}
	
	public RevengeSoldierInfo(String playerId) {
		this.uuid = HawkUUIDGenerator.genUUID();
		this.playerId = playerId;
		deadSoldier = new HashMap<Integer, Integer>();
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public Map<Integer, Integer> getDeadSoldier() {
		return deadSoldier;
	}

	public void setDeadSoldier(Map<Integer, Integer> deadSoldier) {
		this.deadSoldier = deadSoldier;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public long getSoldierDeadTime() {
		return soldierDeadTime;
	}

	public void setSoldierDeadTime(long soldierDeadTime) {
		this.soldierDeadTime = soldierDeadTime;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

}

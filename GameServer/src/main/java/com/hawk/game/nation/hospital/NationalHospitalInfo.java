package com.hawk.game.nation.hospital;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.os.HawkOSOperator;

/**
 * 国家医院相关信息
 * 
 * @author lating
 *
 */
public class NationalHospitalInfo {
	/**
	 * 玩家ID
	 */
	private String playerId;
	/**
	 * 上一次计算复活的时间
	 */
	private long lastCalcTime;
	/**
	 * 上次计算到哪个死兵ID了
	 */
	private int armyId;
	/**
	 * 玩家预设的死兵
	 */
	private Map<Integer, Integer> presetDeleteSoldier;
	/**
	 * 复活所有的死兵结束时间
	 */
	private long recoverEndTime;
	/**
	 * 复活队列开始时间
	 */
	private long queueStartTime;
	/**
	 * 复活队列的队列ID
	 */
	private String queueUuid;
	/**
	 * 加速减少的时间
	 */
	private long totalReduceTime;
	
	/**
	 * 优先恢复的兵
	 */
	private List<Integer> firstRecoverArmy;
	
	public NationalHospitalInfo() {
	}
	
	public NationalHospitalInfo(String playerId) {
		this.playerId = playerId;
		this.presetDeleteSoldier = new HashMap<Integer, Integer>();
		this.queueUuid = HawkOSOperator.randomUUID();
		this.firstRecoverArmy = new ArrayList<>();
	}
	
	public String getPlayerId() {
		return playerId;
	}
	
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	
	public long getLastCalcTime() {
		return lastCalcTime;
	}
	
	public void setLastCalcTime(long lastCalcTime) {
		this.lastCalcTime = lastCalcTime;
	}
	
	public Map<Integer, Integer> getPresetDeleteSoldier() {
		return presetDeleteSoldier;
	}
	
	public void setPresetDeleteSoldier(Map<Integer, Integer> presetDeleteSoldier) {
		this.presetDeleteSoldier = presetDeleteSoldier;
	}
	
	public List<Integer> getFirstRecoverArmy() {
		if (firstRecoverArmy == null) {
			firstRecoverArmy = new ArrayList<>();
		}
		return firstRecoverArmy;
	}

	public void setFirstRecoverArmy(List<Integer> firstRecoverArmy) {
		this.firstRecoverArmy = firstRecoverArmy;
	}
	
	public void clearFirstRecoverArmy() {
		this.getFirstRecoverArmy().clear();
	}
	
	public void addFirstRecoverArmy(int armyType) {
		if (!this.getFirstRecoverArmy().contains(armyType)) {
			this.getFirstRecoverArmy().add(armyType);
		}
	}

	public int getArmyId() {
		return armyId;
	}

	public void setArmyId(int armyId) {
		this.armyId = armyId;
	}

	public long getRecoverEndTime() {
		return recoverEndTime;
	}

	public void setRecoverEndTime(long recoverEndTime) {
		this.recoverEndTime = recoverEndTime;
	}
	
	public long getQueueStartTime() {
		return queueStartTime;
	}

	public void setQueueStartTime(long queueStartTime) {
		this.queueStartTime = queueStartTime;
	}
	
	public String getQueueUuid() {
		return queueUuid;
	}

	public void setQueueUuid(String queueUuid) {
		this.queueUuid = queueUuid;
	}

	public long getTotalReduceTime() {
		return totalReduceTime;
	}

	public void setTotalReduceTime(long totalReduceTime) {
		this.totalReduceTime = totalReduceTime;
	}
	
}

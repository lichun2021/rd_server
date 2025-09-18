package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 资源被偷取事件
 * @author golden
 *
 */
public class ResourceDefenseBeStealEvent extends ActivityEvent {
	
	/**
	 * 偷取方资源站等级
	 */
	public int tarStationLevel;
	
	/**
	 * 资源站id
	 */
	public int stationId;
	
	/**
	 * 资源id
	 */
	public int resId;
	
	/**
	 * 目标玩家id
	 */
	public String targetPlayerId;
	
	public ResourceDefenseBeStealEvent(){ super(null);}
	public ResourceDefenseBeStealEvent(String playerId, int tarStationLevel, int stationId, int resId, String targetPlayerId) {
		super(playerId);
		this.tarStationLevel = tarStationLevel;
		this.stationId = stationId;
		this.resId = resId;
		this.targetPlayerId = targetPlayerId;
	}

	public int getTarStationLevel() {
		return tarStationLevel;
	}

	public int getStationId() {
		return stationId;
	}

	public void setStationId(int stationId) {
		this.stationId = stationId;
	}

	public int getResId() {
		return resId;
	}

	public String getTargetPlayerId() {
		return targetPlayerId;
	}

	public void setTargetPlayerId(String targetPlayerId) {
		this.targetPlayerId = targetPlayerId;
	}
	
}

package com.hawk.game.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 限时商店条件信息
 * 
 * @author lating
 *
 */
public class TimeLimitStoreConditionInfo {
	/**
	 * 玩家角色ID
	 */
	private String playerId;
	/**
	 * 触发类型
	 */
	private int triggerType;
	/**
	 * 条件状态
	 */
	private ConditionState state;
	/**
	 * 有效积累开始时间
	 */
	private long startTime;
	/**
	 * 已积累的数量
	 */
	private long totalCount;
    /**
     * 每个时间点完成的数量
     */
	private Map<Long, Integer> timeFinishCount;
	
	public enum ConditionState {
		INIT,     // 原始积累期
		COMPLETE, // 条件已达成
		SELL,     // 上架出售中
		END,      // 出售完或超时结束
	}
	
	public TimeLimitStoreConditionInfo() {
		this.timeFinishCount = new HashMap<Long, Integer>();
	}
	
	public TimeLimitStoreConditionInfo(String playerId, int triggerType) {
		this.playerId = playerId;
		this.state = ConditionState.INIT;
		this.triggerType = triggerType;
		this.timeFinishCount = new HashMap<Long, Integer>();
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getTriggerType() {
		return triggerType;
	}

	public void setTriggerType(int triggerType) {
		this.triggerType = triggerType;
	}

	public ConditionState getState() {
		return state;
	}

	public void setState(ConditionState state) {
		this.state = state;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

	public Map<Long, Integer> getTimeFinishCount() {
		return timeFinishCount;
	}

	public TimeLimitStoreConditionInfo copy() {
		TimeLimitStoreConditionInfo newObject = new TimeLimitStoreConditionInfo(this.playerId, this.triggerType);
		newObject.setStartTime(this.startTime);
		newObject.setState(this.state);
		newObject.setTotalCount(this.totalCount);
		for (Entry<Long, Integer> entry : this.timeFinishCount.entrySet()) {
			newObject.getTimeFinishCount().put(entry.getKey(), entry.getValue());
		}
		
		return newObject;
	}
}

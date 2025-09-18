package com.hawk.game.entity;

/**
 * 全局buff 入redis
 * @author jm
 *
 */
public class GlobalBuffEntity {
	/**
	 * 状态Id
	 */
	private int statusId;
	/**
	 * 作用值
	 */
	private int value;	
	/**
	 * 结束时间
	 */
	private long endTime;
	/**
	 * 开始时间
	 */
	private long startTime;
	public int getStatusId() {
		return statusId;
	}
	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
	
}

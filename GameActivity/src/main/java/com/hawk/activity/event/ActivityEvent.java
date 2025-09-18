package com.hawk.activity.event;

import java.io.Serializable;

/**
 * 活动事件抽象
 * @author PhilChen
 *
 */
public abstract class ActivityEvent implements Serializable {

	private static final long serialVersionUID = 20200316151200001l;

	private String playerId;

	// 事件发生的时间
	private long eventTime;

	// 是否跳过拦截
	private boolean skip;
	// 活动类型（不需要时默认为0）
	private int activityType;

	public ActivityEvent(String playerId) {
		this.playerId = playerId;
	}

	public ActivityEvent(String playerId, boolean skip) {
		this.playerId = playerId;
		this.skip = skip;
	}

	public String getPlayerId() {
		return playerId;
	}

	public boolean isSkip() {
		return skip;
	}
	
	public int getActivityType() {
		return activityType;
	}

	public void setActivityType(int activityType) {
		this.activityType = activityType;
	}

	/**
	 * 玩家离线推送时,是否存储处理
	 * 
	 * @return
	 */
	public boolean isOfflineResent() {
		return false;
	}

	public long getEventTime() {
		return eventTime;
	}

	public void setEventTime(long eventTime) {
		this.eventTime = eventTime;
	}

	@SuppressWarnings("unchecked")
	public <T> T convert() {
		return (T) this;
	}

}

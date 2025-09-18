package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 队列加速事件
 * @author PhilChen
 *
 */
public class QueueSpeedUpEvent extends ActivityEvent {
	
	private int queueType;
	/** 加速时间(毫秒)*/
	private long upTime;

	public QueueSpeedUpEvent(){ super(null);}
	public QueueSpeedUpEvent(String playerId, int queueType, long upTime) {
		super(playerId);
		this.queueType = queueType;
		this.upTime = upTime;
	}
	
	public int getQueueType() {
		return queueType;
	}
	
	public long getUpTime() {
		return upTime;
	}
}

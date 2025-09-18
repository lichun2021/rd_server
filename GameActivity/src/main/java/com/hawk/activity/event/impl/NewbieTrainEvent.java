package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 新兵作训操作事件
 * @author lating
 */
public class NewbieTrainEvent extends ActivityEvent {

	private int type;
	private int times;
	
	public NewbieTrainEvent(){ 
		super(null);
	}
	
	public NewbieTrainEvent(String playerId, int type, int times) {
		super(playerId);
		this.type = type;
		this.times = times;
	}
	
	public int getType() {
		return type;
	}

	public int getTimes() {
		return times;
	}
}

package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 制造陷阱完成事件
 * 
 * @author lating
 *
 */
public class MakeTrapEvent extends ActivityEvent {
	
	private int trapId;
	
	private int count;

	public MakeTrapEvent(){ super(null);}
	public MakeTrapEvent(String playerId, int trapId, int count) {
		super(playerId);
		this.trapId = trapId;
		this.count = count;
	}

	public int getTrapId() {
		return trapId;
	}

	public int getCount() {
		return count;
	}

}

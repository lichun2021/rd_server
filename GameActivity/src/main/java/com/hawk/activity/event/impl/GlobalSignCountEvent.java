package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 八日庆典增加积分事件
 * @author PhilChen
 *
 */
public class GlobalSignCountEvent extends ActivityEvent {

	private int count;
	
	public GlobalSignCountEvent(){ super(null);}
	public GlobalSignCountEvent(String playerId, int count) {
		super(playerId);
		this.count = count;
	}
	
	public int getCount() {
		return count;
	}

}

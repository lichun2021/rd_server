package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 八日庆典2增加积分事件
 * @author Jesse
 *
 */
public class LoverMeetEndingCountEvent extends ActivityEvent {

	private int count;
	
	public LoverMeetEndingCountEvent(){ super(null);}
	public LoverMeetEndingCountEvent(String playerId, int count) {
		super(playerId);
		this.count = count;
	}
	
	public int getCount() {
		return count;
	}

}

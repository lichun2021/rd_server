package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 培养英雄(使用英雄经验道具)
 * @author golden
 *
 */
public class UseHeroExpItemEvent extends ActivityEvent {

	private int count;
	
	public UseHeroExpItemEvent(){ super(null);}
	public UseHeroExpItemEvent(String playerId, int count) {
		super(playerId);
		this.count = count;
	}

	public int getCount() {
		return count;
	}
}

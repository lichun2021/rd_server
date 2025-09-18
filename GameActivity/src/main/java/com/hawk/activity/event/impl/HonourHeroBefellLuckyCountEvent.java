package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 
 * @author che
 *
 */
public class HonourHeroBefellLuckyCountEvent extends ActivityEvent {

	
	private int count;

	public HonourHeroBefellLuckyCountEvent(){ super(null);}
	public HonourHeroBefellLuckyCountEvent(String playerId, int count) {
		super(playerId);
		this.count = count;
	}

	public int getCount() {
		return count;
	}

	

}

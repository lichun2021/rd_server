package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 
 * @author che
 *
 */
public class HonourMobilizeCountEvent extends ActivityEvent {

	
	private int count;

	public HonourMobilizeCountEvent(){ super(null);}
	public HonourMobilizeCountEvent(String playerId, int count) {
		super(playerId);
		this.count = count;
	}

	public int getCount() {
		return count;
	}

	

}

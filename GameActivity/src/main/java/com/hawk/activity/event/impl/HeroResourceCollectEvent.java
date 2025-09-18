package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 英雄 采集行军.
 * @author jm
 *
 */
public class HeroResourceCollectEvent extends ActivityEvent{

	public HeroResourceCollectEvent(){ super(null);}
	public HeroResourceCollectEvent(String playerId) {
		super(playerId);
	}
	
	@Override
	public boolean isOfflineResent() {
		return true;
	}
	
}

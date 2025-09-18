package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 英雄数量/等级/星级变更
 * @author PhilChen
 *
 */
public class HeroChangeEvent extends ActivityEvent {

	public HeroChangeEvent(){ super(null);}
	public HeroChangeEvent(String playerId) {
		super(playerId);
	}

}

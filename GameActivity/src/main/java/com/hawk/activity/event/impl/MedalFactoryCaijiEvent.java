package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 勋章工厂生产
 *
 */
public class MedalFactoryCaijiEvent extends ActivityEvent {

	public MedalFactoryCaijiEvent() {
		super(null);
	}

	public MedalFactoryCaijiEvent(String playerId) {
		super(playerId);
	}

}

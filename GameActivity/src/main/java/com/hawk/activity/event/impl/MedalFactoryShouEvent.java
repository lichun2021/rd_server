package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 勋章工厂生产
 *
 */
public class MedalFactoryShouEvent extends ActivityEvent {

	public MedalFactoryShouEvent() {
		super(null);
	}

	public MedalFactoryShouEvent(String playerId) {
		super(playerId);
	}

}

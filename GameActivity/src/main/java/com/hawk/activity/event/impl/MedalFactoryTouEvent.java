package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.EvolutionEvent;

/**
 * 勋章工厂生产
 *
 */
public class MedalFactoryTouEvent extends ActivityEvent implements EvolutionEvent{

	public MedalFactoryTouEvent() {
		super(null);
	}

	public MedalFactoryTouEvent(String playerId) {
		super(playerId);
	}

}

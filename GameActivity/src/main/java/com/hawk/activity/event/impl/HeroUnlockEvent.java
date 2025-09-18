package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;
import com.hawk.activity.event.speciality.SpaceMechaEvent;

/**
 * 英雄解锁
 * 
 * @author Jesse
 *
 */
public class HeroUnlockEvent extends ActivityEvent implements OrderEvent, SpaceMechaEvent {

	private int quality;

	public HeroUnlockEvent(){ super(null);}
	public HeroUnlockEvent(String playerId, int quality) {
		super(playerId);
		this.quality = quality;
	}

	public final int getQuality() {
		return quality;
	}

}

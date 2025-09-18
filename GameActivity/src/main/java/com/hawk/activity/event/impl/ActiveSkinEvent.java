package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 激活永久皮肤
 * @author Jesse
 *
 */
public class ActiveSkinEvent extends ActivityEvent implements OrderEvent {
	private int dressType;
	private int modelType;
	private int continueSeconds;

	public ActiveSkinEvent(){ super(null);}
	public ActiveSkinEvent(String playerId, int dressType, int modelType, int continueSeconds) {
		super(playerId);
		this.dressType = dressType;
		this.modelType = modelType;
		this.continueSeconds = continueSeconds;
	}

	public final int getDressType() {
		return dressType;
	}

	public final int getModelType() {
		return modelType;
	}

	public final int getContinueSeconds() {
		return continueSeconds;
	}

	public boolean isForever() {
		return continueSeconds == 0;
	}
}

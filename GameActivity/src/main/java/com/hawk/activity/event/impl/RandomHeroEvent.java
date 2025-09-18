package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 抽英雄事件
 * @author PhilChen
 *
 */
public class RandomHeroEvent extends ActivityEvent implements OrderEvent {

	/** 次数 */
	private int count;

	private int gachaType;

	public RandomHeroEvent(){ super(null);}
	public RandomHeroEvent(String playerId, int count, int gachaType) {
		super(playerId);
		this.count = count;
		this.gachaType = gachaType;
	}

	public int getCount() {
		return count;
	}

	public int getGachaType() {
		return gachaType;
	}
}

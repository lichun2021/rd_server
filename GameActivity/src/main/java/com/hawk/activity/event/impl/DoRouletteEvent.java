package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 转时空轮盘事件
 * @author Jesse
 *
 */
public class DoRouletteEvent extends ActivityEvent implements OrderEvent {
	private int count;

	public DoRouletteEvent(){ super(null);}
	public DoRouletteEvent(String playerId, int count) {
		super(playerId);
		this.count = count;
	}

	public int getCount() {
		return count;
	}

}

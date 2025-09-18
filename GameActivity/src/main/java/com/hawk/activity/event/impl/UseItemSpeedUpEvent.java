package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 使用道具加速分钟数
 * @author golden
 *
 */
public class UseItemSpeedUpEvent extends ActivityEvent implements OrderEvent{

	private int minute;

	public UseItemSpeedUpEvent(){ super(null);}
	public UseItemSpeedUpEvent(String playerId, int minute) {
		super(playerId);
		this.minute = minute;
	}

	public int getMinute() {
		return minute;
	}
}

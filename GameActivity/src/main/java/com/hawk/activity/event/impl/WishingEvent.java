package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 许愿池许愿事件
 * 
 * @author lating
 *
 */
public class WishingEvent extends ActivityEvent implements OrderEvent{
	
	public WishingEvent(){ super(null);}
	public WishingEvent(String playerId) {
		super(playerId);
	}
}

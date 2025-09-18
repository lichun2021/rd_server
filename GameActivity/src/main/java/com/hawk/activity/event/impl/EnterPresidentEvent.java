package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 进入总统府事件
 * @author Jesse
 *
 */
public class EnterPresidentEvent extends ActivityEvent implements OrderEvent {
	
	public EnterPresidentEvent(){ super(null);}
	public EnterPresidentEvent(String playerId) {
		super(playerId);
	}
}

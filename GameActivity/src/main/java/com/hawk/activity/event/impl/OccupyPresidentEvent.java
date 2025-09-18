package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 占领总统府事件
 * @author Jesse
 *
 */
public class OccupyPresidentEvent extends ActivityEvent implements OrderEvent {
	
	public OccupyPresidentEvent(){ super(null);}
	public OccupyPresidentEvent(String playerId) {
		super(playerId);
	}
}

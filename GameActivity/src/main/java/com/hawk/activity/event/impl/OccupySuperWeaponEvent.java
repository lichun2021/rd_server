package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 占领战区事件
 * @author Jesse
 *
 */
public class OccupySuperWeaponEvent extends ActivityEvent implements OrderEvent {
	
	private boolean isFinally;
	
	public OccupySuperWeaponEvent(){ super(null);}
	public OccupySuperWeaponEvent(String playerId, boolean isFinally) {
		super(playerId);
		this.isFinally = isFinally;
	}

	public final boolean isFinally() {
		return isFinally;
	}
	
}

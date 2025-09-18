package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 进入战区事件
 * @author Jesse
 *
 */
public class EnterSuperWeaponEvent extends ActivityEvent implements OrderEvent {
	
	public EnterSuperWeaponEvent(){ super(null);}
	public EnterSuperWeaponEvent(String playerId) {
		super(playerId);
	}
}

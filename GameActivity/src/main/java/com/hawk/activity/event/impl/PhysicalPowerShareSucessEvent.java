

package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 坦克工厂分享
 * @author che
 *
 */
public class PhysicalPowerShareSucessEvent  extends ActivityEvent {

	
	
	public PhysicalPowerShareSucessEvent(){ super(null);}
	public PhysicalPowerShareSucessEvent (String playerId) {
		super(playerId);
	}
	
	

	public static PhysicalPowerShareSucessEvent valueOf(String playerId){
		PhysicalPowerShareSucessEvent event = new PhysicalPowerShareSucessEvent(playerId);
		return event;
	}

	
	
	
}

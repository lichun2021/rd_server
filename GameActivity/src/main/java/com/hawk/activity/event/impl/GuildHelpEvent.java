package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.EvolutionEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 联盟帮助事件
 * @author PhilChen
 *
 */
public class GuildHelpEvent extends ActivityEvent implements EvolutionEvent,OrderEvent {
	
	public GuildHelpEvent(){ super(null);}
	public GuildHelpEvent(String playerId) {
		super(playerId);
	}

}

package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;
import com.hawk.activity.event.speciality.SpaceMechaEvent;

/**
 * 联盟捐献事件
 * @author PhilChen
 *
 */
public class GuildDonateEvent extends ActivityEvent implements OrderEvent, SpaceMechaEvent {
	
	public GuildDonateEvent(){ super(null);}
	public GuildDonateEvent(String playerId) {
		super(playerId);
	}
	
}

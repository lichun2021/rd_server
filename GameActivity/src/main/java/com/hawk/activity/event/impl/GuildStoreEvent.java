package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 开启联盟宝藏事件
 * 
 * @author golden
 *
 */
public class GuildStoreEvent extends ActivityEvent implements OrderEvent {

	public GuildStoreEvent(){ super(null);}
	public GuildStoreEvent(String playerId) {
		super(playerId);
	}
}

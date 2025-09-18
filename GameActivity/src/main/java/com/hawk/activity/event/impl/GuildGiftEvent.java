package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 领取联盟礼物数量
 * 
 * @author Jesse
 *
 */
public class GuildGiftEvent extends ActivityEvent implements OrderEvent {

	private int count;

	public GuildGiftEvent(){ super(null);}
	public GuildGiftEvent(String playerId, int count) {
		super(playerId);
		this.count = count;
	}

	public int getCount() {
		return count;
	}

}

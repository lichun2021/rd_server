package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class PowerLabItemDropAchieveEvent extends ActivityEvent {
	
	private int itemId;
	
	private int count;
	
	public PowerLabItemDropAchieveEvent(){ super(null);}
	public PowerLabItemDropAchieveEvent(String playerId, int itemId, int count) {
		super(playerId);
		this.itemId = itemId;
		this.count = count;
	}

	public int getItemId() {
		return itemId;
	}
	
	public int getCount() {
		return count;
	}

}

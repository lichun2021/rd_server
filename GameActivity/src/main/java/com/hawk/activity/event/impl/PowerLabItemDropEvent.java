package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/***
 * 能量道具掉落事件
 * @author yang.rao
 *
 */
public class PowerLabItemDropEvent extends ActivityEvent implements OrderEvent{

	private int itemId;
	
	private int count;
	
	public PowerLabItemDropEvent(){ super(null);}
	public PowerLabItemDropEvent(String playerId, int itemId, int count) {
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

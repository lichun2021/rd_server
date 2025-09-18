package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/***
 * 荣耀勋章掉落事件
 * @author yang.rao
 *
 */
public class HonorItemDropEvent extends ActivityEvent implements OrderEvent{

	private int count;
	
	public HonorItemDropEvent(){ super(null);}
	public HonorItemDropEvent(String playerId, int count) {
		super(playerId);
		this.count = count;
	}
	
	public int getCount() {
		return count;
	}
}

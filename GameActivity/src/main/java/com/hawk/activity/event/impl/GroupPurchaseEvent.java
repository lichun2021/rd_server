package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 跨服团购刷新事件
 * 
 * @author Jesse
 *
 */
public class GroupPurchaseEvent extends ActivityEvent {

	public GroupPurchaseEvent(){ super(null);}
	public GroupPurchaseEvent(String playerId) {
		super(playerId);
	}

}

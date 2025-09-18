package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 一元购直购事件
 * 
 * @author lating
 *
 */
public class OneRMBPurchaseEvent extends ActivityEvent {

	public OneRMBPurchaseEvent(){ super(null);}
	public OneRMBPurchaseEvent(String playerId) {
		super(playerId, true);
	}
}

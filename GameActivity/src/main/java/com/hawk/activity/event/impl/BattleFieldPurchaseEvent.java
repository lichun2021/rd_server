package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 战地寻宝直购事件
 */
public class BattleFieldPurchaseEvent extends ActivityEvent {

	private String giftId;
	
	
	public BattleFieldPurchaseEvent(){ super(null);}
	public BattleFieldPurchaseEvent(String playerId, String giftId) {
		super(playerId, true);
		this.giftId = giftId;
	}

	public String getGiftId() {
		return giftId;
	}
}

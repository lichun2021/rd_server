package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 装备黑市直购事件
 * @author che
 *
 */
public class EquipBlackMarketBuyEvent extends ActivityEvent {

	private String giftId;
	
	
	public EquipBlackMarketBuyEvent(){ super(null);}
	public EquipBlackMarketBuyEvent(String playerId, String giftId) {
		super(playerId, true);
		this.giftId = giftId;
	}

	public String getGiftId() {
		return giftId;
	}
}

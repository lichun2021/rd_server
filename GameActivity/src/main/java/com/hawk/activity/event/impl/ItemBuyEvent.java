package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 物品购买事件
 * @author PhilChen
 *
 */
public class ItemBuyEvent extends ActivityEvent {

	/** 物品id*/
	private int itemId;

	/** 购买数量*/
	private int buyNum;

	public ItemBuyEvent(){ super(null);}
	public ItemBuyEvent(String playerId, int itemId, int buyNum) {
		super(playerId);
		this.itemId = itemId;
		this.buyNum = buyNum;
	}

	public int getItemId() {
		return itemId;
	}

	public int getBuyNum() {
		return buyNum;
	}

}

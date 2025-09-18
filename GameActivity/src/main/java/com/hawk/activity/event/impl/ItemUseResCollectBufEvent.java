package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 
 * @author PhilChen
 *
 */
public class ItemUseResCollectBufEvent extends ActivityEvent {
	
	private int itemId;
	private int itemCount;

	public ItemUseResCollectBufEvent(){ super(null);}
	public ItemUseResCollectBufEvent(String playerId, int itemId, int itemCount) {
		super(playerId);
		this.itemId = itemId;
	}

	public int getItemId() {
		return itemId;
	}
	
	public int getItemCount() {
		return itemCount;
	}

}

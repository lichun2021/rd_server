package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 赛季拼图赠送碎片
 * @author lating
 */
public class SeasonPuzzleSendItemEvent extends ActivityEvent {

	private String callHelpId;
	private int itemId;
	
	public SeasonPuzzleSendItemEvent(){ super(null);}
	public SeasonPuzzleSendItemEvent(String playerId, String callHelpId, int itemId) {
		super(playerId);
		this.callHelpId = callHelpId;
		this.itemId = itemId;
	}
	
	public int getItemId() {
		return itemId;
	}
	
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	
	public String getCallHelpId() {
		return callHelpId;
	}
	
	public void setCallHelpId(String callHelpId) {
		this.callHelpId = callHelpId;
	}
	
}

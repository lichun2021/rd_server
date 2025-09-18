package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 有福同享376活动给老服角色返利事件
 * @author lating
 *
 */
public class ShareProsperityEvent extends ActivityEvent {

	/** 钻石数量*/
	private int diamondNum;
	
	public ShareProsperityEvent(){ super(null);}
	public ShareProsperityEvent(String playerId, int diamondNum) {
		super(playerId, true);
		this.diamondNum = diamondNum;
	}
	
	public int getDiamondNum() {
		return diamondNum;
	}
	
	@Override
	public boolean isSkip() {
		return true;
	}
}

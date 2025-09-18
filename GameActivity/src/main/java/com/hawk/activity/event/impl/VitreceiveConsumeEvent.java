package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 消耗体力事件
 * @author PhilChen
 *
 */
public class VitreceiveConsumeEvent extends ActivityEvent {

	/** 消耗值*/
	private int consumeValue;

	public VitreceiveConsumeEvent(){ super(null);}
	public VitreceiveConsumeEvent(String playerId, int consumeValue) {
		super(playerId);
		this.consumeValue = consumeValue;
	}

	public int getConsumeValue() {
		return consumeValue;
	}
}

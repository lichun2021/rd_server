package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 消耗金币/金条事件
 * @author PhilChen
 *
 */
public class ConsumeMoneyEvent extends ActivityEvent implements OrderEvent{

	private int resType;
	/** 数量*/
	private long num;

	public ConsumeMoneyEvent(){ super(null);}
	public ConsumeMoneyEvent(String playerId, int resType, long num) {
		super(playerId);
		this.resType = resType;
		this.num = num;
	}

	public int getResType() {
		return resType;
	}
	
	public long getNum() {
		return num;
	}

}

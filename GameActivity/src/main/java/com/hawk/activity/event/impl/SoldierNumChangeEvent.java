package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 兵种数量变化事件
 * @author PhilChen
 *
 */
public class SoldierNumChangeEvent extends ActivityEvent {
	
	/** 兵种id*/
	private int trainId;
	
	/** 数量*/
	private int num;

	public SoldierNumChangeEvent(){ super(null);}
	public SoldierNumChangeEvent(String playerId, int trainId, int num) {
		super(playerId);
		this.num = num;
		this.trainId = trainId;
	}
	
	public int getNum() {
		return num;
	}

	public int getTrainId() {
		return trainId;
	}

}

package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 兵种训练开始事件
 * @author PhilChen
 *
 */
public class TrainSoldierStartEvent extends ActivityEvent {
	
	/** 兵种类型*/
	private int type;
	
	/** 兵种id*/
	private int soldierId;
	
	/** 训练数量*/
	private int num;

	public TrainSoldierStartEvent(){ super(null);}
	public TrainSoldierStartEvent(String playerId, int type, int soldierId, int num) {
		super(playerId);
		this.type = type;
		this.num = num;
		this.soldierId = soldierId;
	}
	
	public int getType() {
		return type;
	}
	
	public int getNum() {
		return num;
	}

	public int getSoldierId() {
		return soldierId;
	}

}

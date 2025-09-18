package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.EvolutionEvent;
import com.hawk.activity.event.speciality.OrderEvent;
import com.hawk.activity.event.speciality.SpaceMechaEvent;
import com.hawk.activity.event.speciality.StrongestEvent;

/**
 * 兵种训练完成事件
 * @author PhilChen
 *
 */
public class TrainSoldierCompleteEvent extends ActivityEvent  implements StrongestEvent, OrderEvent, EvolutionEvent, SpaceMechaEvent {
	
	/** 兵种类型*/
	private int type;
	
	/** 兵种id*/
	private int trainId;
	
	/** 训练数量*/
	private int num;

	/** 士兵等级*/
	private int level;
	
	public TrainSoldierCompleteEvent(){ super(null);}
	public TrainSoldierCompleteEvent(String playerId, int type, int trainId, int num, int level) {
		super(playerId);
		this.type = type;
		this.num = num;
		this.trainId = trainId;
		this.level = level;
	}
	
	public int getType() {
		return type;
	}
	
	public int getNum() {
		return num;
	}

	public int getTrainId() {
		return trainId;
	}

	public int getLevel() {
		return level;
	}

	@Override
	public String toString() {
		return "TrainSoldierComplete [type=" + type + ", trainId=" + trainId + ", num=" + num + ", level=" + level
				+ "]";
	}
	
}

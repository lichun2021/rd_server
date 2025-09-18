package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 建筑建造事件
 * @author PhilChen
 *
 */
public class BuildingCreateEvent extends ActivityEvent {
	
	/** 建筑类型*/
	private int buildType;
	
	/** 该建筑类型当前数量*/
	private int num;

	public BuildingCreateEvent(){ super(null);}
	public BuildingCreateEvent(String playerId, int buildType, int num) {
		super(playerId);
		this.num = num;
		this.buildType = buildType;
	}
	
	public int getBuildType() {
		return buildType;
	}
	
	public int getNum() {
		return num;
	}

}

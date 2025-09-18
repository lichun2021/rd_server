package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 推广员活动专用的建筑升级
 * 
 * @author RickMei 
 *
 */
public class BuildingLevelUpSpreadEvent extends ActivityEvent{

	/** 建筑等级 */
	private int buildType;

	/** 建筑当前等级 */
	private int level;

	public BuildingLevelUpSpreadEvent(){ super(null);}
	public BuildingLevelUpSpreadEvent(String playerId, int buildType, int level) {
		super(playerId);
		this.level = level;
		this.buildType = buildType;
	}


	public int getBuildType() {
		return buildType;
	}

	public int getLevel() {
		return level;
	}
}

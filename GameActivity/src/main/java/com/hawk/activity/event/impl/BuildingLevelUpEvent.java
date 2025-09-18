package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 建筑等级提升事件
 * 
 * @author PhilChen
 *
 */
public class BuildingLevelUpEvent extends ActivityEvent implements OrderEvent{

	/** 建筑类型 */
	private int buildType;

	/** 建筑当前等级 */
	private int level;
	/**
	 * 建筑荣耀等级的阶段
	 */
	private int progress;

	/** 是否登录时兼容处理 */
	private boolean isLogin;

	/** 是否推广员发送的事件 */
	private boolean isSpread = false;

	/** 增加的战力 */
	private int addPower;

	public BuildingLevelUpEvent(){ super(null);}
	public BuildingLevelUpEvent(String playerId, int buildType, int level, int progress, boolean isLogin, int addPower) {
		super(playerId);
		this.level = level;
		this.buildType = buildType;
		this.isSpread = false;
		this.isLogin = isLogin;
		this.addPower = addPower;
		this.progress = progress;
	}

	public int getBuildType() {
		return buildType;
	}

	public int getLevel() {
		return level;
	}

	public boolean getIsSpread() {
		return this.isSpread;
	}

	public boolean isLogin() {
		return isLogin;
	}

	public int getAddPower() {
		return addPower;
	}
	
	public int getProgress() {
		return progress;
	}
	
	@Override
	public boolean isOfflineResent() {
		return true;
	}

}

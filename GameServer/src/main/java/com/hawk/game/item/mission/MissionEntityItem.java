package com.hawk.game.item.mission;

import com.hawk.game.util.GsConst.MissionState;

/**
 * 任务实体结构接口
 * 
 * @author golden
 *
 */
public class MissionEntityItem {

	/** 配置id*/
	protected int cfgId;

	/** 进度值*/
	protected long value;

	/** 状态 0 未完成 1已完成 2已领奖*/
	protected int state;

	public MissionEntityItem() {
		
	}
	
	public MissionEntityItem(int cfgId, int value, int state) {
		this.cfgId = cfgId;
		this.value = value;
		this.state = state;
	}

	public int getCfgId() {
		return cfgId;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	public void addValue(int value) {
		this.value += value;
	}
	
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String toString() {
		return String.format("%d_%d_%d", cfgId, value, state);
	}

	/**
	 * 任务是否完成
	 */
	public void isMissionComplete() {
		this.state = MissionState.STATE_FINISH;
	}
	
	/**
	 * 任务是否领奖
	 */
	public void isMissionBonus() {
		this.state = MissionState.STATE_BONUS;
	}
}

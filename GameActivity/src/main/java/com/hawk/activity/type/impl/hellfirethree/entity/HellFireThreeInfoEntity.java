package com.hawk.activity.type.impl.hellfirethree.entity;

import java.util.List;


/**
 * 记录当前活动的一些信息
 * @author jm
 *
 */
public class HellFireThreeInfoEntity {
	/**
	 * 周期ID
	 */
	private int cycleId;
	/**
	 * 目标唯一ID
	 */
	private List<Integer> targetCfgIdList;
	/**
	 * 周期ID列表
	 */
	private List<Integer> cycleIdList;
	/**
	 * 活动的周期次数
	 */
	private int cycleStartTime;
	
	public int getCycleStartTime() {
		return cycleStartTime;
	}
	public void setCycleStartTime(int cycleTimes) {
		this.cycleStartTime = cycleTimes;
	}
	public int getCycleId() {
		return cycleId;
	}
	public void setCycleId(int cycleId) {
		this.cycleId = cycleId;
	}
	public List<Integer> getTargetCfgIdList() {
		return targetCfgIdList;
	}
	public void setTargetCfgIdList(List<Integer> targetCfgIdList) {
		this.targetCfgIdList = targetCfgIdList;
	}
	public List<Integer> getCycleIdList() {
		return cycleIdList;
	}
	public void setCycleIdList(List<Integer> cycleIdList) {
		this.cycleIdList = cycleIdList;
	}
	
	public  HellFireThreeInfoEntity clone() {
		HellFireThreeInfoEntity  entity = new HellFireThreeInfoEntity();
		entity.cycleId = this.cycleId;
		entity.cycleIdList = this.cycleIdList;
		entity.cycleStartTime = this.cycleStartTime;
		entity.targetCfgIdList = this.targetCfgIdList;
		
		return entity;
	}
}

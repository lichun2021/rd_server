package com.hawk.activity.type.impl.hellfiretwo.entity;

import java.util.List;

/**
 * 记录当前活动的一些信息
 * @author jm
 *
 */
public class HellFireTwoInfoEntity {
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
	 * 这一期开始的时间
	 */
	private int startTime;
	
	
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
	
	public  HellFireTwoInfoEntity clone() {
		HellFireTwoInfoEntity  entity = new HellFireTwoInfoEntity();
		entity.cycleId = this.cycleId;
		entity.cycleIdList = this.cycleIdList;
		entity.startTime = this.startTime;
		entity.targetCfgIdList = this.targetCfgIdList;
		
		return entity;
	}
	public int getStartTime() {
		return startTime;
	}
	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}
}

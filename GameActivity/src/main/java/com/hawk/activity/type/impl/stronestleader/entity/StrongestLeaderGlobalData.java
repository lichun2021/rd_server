package com.hawk.activity.type.impl.stronestleader.entity;

public class StrongestLeaderGlobalData {

	/** 当前阶段*/
	private int stageId;
	/** 当前阶段开始时间*/
	private long stageStartTime;
	/** 当前阶段结束时间*/
	private long stageEndTime;
	
	public StrongestLeaderGlobalData() {
	}

	public StrongestLeaderGlobalData(int stageId) {
		this.stageId = stageId;
	}

	public int getStageId() {
		return stageId;
	}

	public void setStageId(int stageId) {
		this.stageId = stageId;
	}

	public long getStageStartTime() {
		return stageStartTime;
	}

	public void setStageStartTime(long stageStartTime) {
		this.stageStartTime = stageStartTime;
	}

	public long getStageEndTime() {
		return stageEndTime;
	}

	public void setStageEndTime(long stageEndTime) {
		this.stageEndTime = stageEndTime;
	}

}

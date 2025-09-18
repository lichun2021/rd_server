package com.hawk.activity.type.impl.strongestGuild.entity;

/***
 * 最强指挥官阶段时间数据
 * @author yang.rao
 *
 */

public class StrongestGuildStageInfo {
	
	private int stageId;
	
	//准备时间点
	private long prepareTime;
	
	private long startTime;
	
	private long endTime;
	
	//下一个阶段id，如果为0则表示没有下一个阶段了
	private int nextStageId;
	
	//上一个阶段id，如果为0则表示没有上一个阶段
	private int beforeStageId;
	
	public StrongestGuildStageInfo(int stageId, long prepareTime, long startTime, long endTime, int nextStageId, int beforeStageId){
		this.stageId = stageId;
		this.prepareTime = prepareTime;
		this.startTime = startTime;
		this.endTime = endTime;
		this.nextStageId = nextStageId;
		this.beforeStageId = beforeStageId;
	}

	public int getStageId() {
		return stageId;
	}

	public long getPrepareTime() {
		return prepareTime;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public long getEndTime() {
		return endTime;
	}

	public int getNextStageId() {
		return nextStageId;
	}

	public int getBeforeStageId() {
		return beforeStageId;
	}
}

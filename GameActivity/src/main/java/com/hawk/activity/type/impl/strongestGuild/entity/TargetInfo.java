package com.hawk.activity.type.impl.strongestGuild.entity;

/***
 * 王者联盟目标信息
 * @author yang.rao
 *
 */
public class TargetInfo {
	
	private int targetId;
	
	private long targetScore;
	
	//初始化的时候大本等级
	private int buildLevel;
	
	private boolean finish = false; //是否完成
	
	private boolean achieve = false; //是否领取
	
	public TargetInfo(){}
	
	public TargetInfo(int targetId, long targetScore, int buildLevel){
		this.targetId = targetId;
		this.targetScore = targetScore;
		this.buildLevel = buildLevel;
	}

	public boolean isFinish() {
		return finish;
	}

	public void setFinish(boolean finish) {
		this.finish = finish;
	}

	public boolean isAchieve() {
		return achieve;
	}

	public void setAchieve(boolean achieve) {
		this.achieve = achieve;
	}

	public int getTargetId() {
		return targetId;
	}

	public long getTargetScore() {
		return targetScore;
	}

	public int getBuildLevel() {
		return buildLevel;
	}
	
	public void setTargetId(int targetId) {
		this.targetId = targetId;
	}

	public void setTargetScore(long targetScore) {
		this.targetScore = targetScore;
	}

	public void setBuildLevel(int buildLevel) {
		this.buildLevel = buildLevel;
	}

	@Override
	public String toString() {
		return "TargetInfo [targetId=" + targetId + ", targetScore=" + targetScore + ", buildLevel=" + buildLevel
				+ ", finish=" + finish + ", achieve=" + achieve + "]";
	}
}

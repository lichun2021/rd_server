package com.hawk.game.module.spacemecha.data;

public class SpaceMechaDataObject {
	/**
	 *  星甲召唤可挑战的最高舱体等级
	 */
	public int spaceMaxLv;
	/**
	 * 星甲召唤已选择的舱体等级
	 */
	public int spaceSelectedLv;
	/**
	 * 星甲召唤舱体放置次数（本期活动内）
	 */
	public int spaceSetTimes;
	/**
	 * 活动周期
	 */
	public int termId;
	/**
	 * 星币数量
	 */
	public long guildPoint;
	
	
	public SpaceMechaDataObject() {
	}
	
	public int getSpaceMaxLv() {
		return spaceMaxLv;
	}

	public void setSpaceMaxLv(int spaceMaxLv) {
		this.spaceMaxLv = spaceMaxLv;
	}
	
	public int getSpaceSelectedLv() {
		return spaceSelectedLv;
	}

	public void setSpaceSelectedLv(int spaceSelectedLv) {
		this.spaceSelectedLv = spaceSelectedLv;
	}

	public int getSpaceSetTimes() {
		return spaceSetTimes;
	}

	public void setSpaceSetTimes(int spaceSetTimes) {
		this.spaceSetTimes = spaceSetTimes;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public long getGuildPoint() {
		return guildPoint;
	}

	public void setGuildPoint(long guildPoint) {
		this.guildPoint = guildPoint;
	}
	
	public void resetData(int termId) {
		this.termId = termId;
		this.guildPoint = 0;
		this.spaceSetTimes = 0;
		this.spaceSelectedLv = 0;
	}
	
}

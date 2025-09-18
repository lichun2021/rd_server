package com.hawk.game.module.lianmengtaiboliya.worldpoint.sub;

import java.util.Map;

import com.hawk.game.protocol.Const.EffType;

public class TBLYAtkBuildBuff {
	private Map<EffType, Integer> buildAtkBuffMap;
	private int npcId;
	private long startTime;
	private long endTime;

	public Map<EffType, Integer> getBuildAtkBuffMap() {
		return buildAtkBuffMap;
	}

	public void setBuildAtkBuffMap(Map<EffType, Integer> buildAtkBuffMap) {
		this.buildAtkBuffMap = buildAtkBuffMap;
	}

	public int getNpcId() {
		return npcId;
	}

	public void setNpcId(int npcId) {
		this.npcId = npcId;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

}

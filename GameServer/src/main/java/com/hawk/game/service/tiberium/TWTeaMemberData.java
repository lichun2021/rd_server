package com.hawk.game.service.tiberium;

import java.util.ArrayList;
import java.util.List;

/**
 *  泰伯利亚小组成员信息
 * @author Jesse
 */
public class TWTeaMemberData {
	public String id;

	public int teamIndex;

	/** 小组策略 */
	public List<Integer> targetList;

	public boolean isCaptain;
	
	public TWTeaMemberData() {
		this.targetList = new ArrayList<>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getTeamIndex() {
		return teamIndex;
	}

	public void setTeamIndex(int teamIndex) {
		this.teamIndex = teamIndex;
	}

	public List<Integer> getTargetList() {
		return targetList;
	}

	public void setTargetList(List<Integer> targetList) {
		this.targetList = targetList;
	}

	public boolean isCaptain() {
		return isCaptain;
	}

	public void setCaptain(boolean isCaptain) {
		this.isCaptain = isCaptain;
	}

}

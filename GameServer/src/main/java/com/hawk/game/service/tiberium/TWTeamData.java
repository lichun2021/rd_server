package com.hawk.game.service.tiberium;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.annotation.JSONField;

/**
 *  泰伯利亚之战小组信息
 * @author Jesse
 */
public class TWTeamData {
	public int teamIndex;

	public String name;

	public Map<String, TWTeaMemberData> memberMap;
	
	public List<Integer> targetList;

	public TWTeamData() {
		this.memberMap = new HashMap<>();
		this.targetList = new ArrayList<>();
	}

	public int getTeamIndex() {
		return teamIndex;
	}

	public void setTeamIndex(int teamIndex) {
		this.teamIndex = teamIndex;
	}

	public String getName() {
		return name == null ? "" : name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, TWTeaMemberData> getMemberMap() {
		return memberMap;
	}

	public void setMemberMap(Map<String, TWTeaMemberData> memberMap) {
		this.memberMap = memberMap;
	}
	
	@JSONField(serialize = false)
	public void resetMemberMap(){
		this.memberMap = new HashMap<>();
	}
	
	public List<Integer> getTargetList() {
		return targetList;
	}

	public void setTargetList(List<Integer> targetList) {
		this.targetList = targetList;
	}

}

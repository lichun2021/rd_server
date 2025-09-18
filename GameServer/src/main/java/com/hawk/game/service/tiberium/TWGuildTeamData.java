package com.hawk.game.service.tiberium;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.annotation.JSONField;

/**
 *  泰伯利亚之战联盟小组信息
 * @author Jesse
 */
public class TWGuildTeamData {
	public String guildId;

	public Map<Integer, TWTeamData> teamInfos;

	public TWGuildTeamData() {
		this.teamInfos = new HashMap<>();
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public Map<Integer, TWTeamData> getTeamInfos() {
		return teamInfos;
	}

	public void setTeamInfos(Map<Integer, TWTeamData> teamInfos) {
		this.teamInfos = teamInfos;
	}
	
	@JSONField(serialize = false)
	public TWTeamData getTwTeamData(int teamIndex){
		return teamInfos.get(teamIndex);
	}
	
	@JSONField(serialize = false)
	public void updataTwTeamData(TWTeamData teamData){
		teamInfos.put(teamData.getTeamIndex(), teamData);
	}
}

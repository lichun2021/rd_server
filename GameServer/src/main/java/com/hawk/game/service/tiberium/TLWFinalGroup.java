package com.hawk.game.service.tiberium;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hawk.game.service.tiberium.TiberiumConst.TLWGroupType;
import com.hawk.game.service.tiberium.TiberiumConst.TLWWarStage;

public class TLWFinalGroup {
	public int season;

	public int termId;
	
	public TLWGroupType groupType;

	public List<String> guildIds;

	/** 各阶段对战信息 */
	public Map<TLWWarStage, List<TLWBattleData>> battleMap;

	public TLWFinalGroup() {
		guildIds = new ArrayList<>();
		battleMap = new HashMap<>();
	}

	public int getSeason() {
		return season;
	}

	public void setSeason(int season) {
		this.season = season;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public TLWGroupType getGroupType() {
		return groupType;
	}

	public void setGroupType(TLWGroupType groupType) {
		this.groupType = groupType;
	}

	public List<String> getGuildIds() {
		return guildIds;
	}

	public void setGuildIds(List<String> guildIds) {
		this.guildIds = guildIds;
	}

	public Map<TLWWarStage, List<TLWBattleData>> getBattleMap() {
		return battleMap;
	}

	public void setBattleMap(Map<TLWWarStage, List<TLWBattleData>> battleMap) {
		this.battleMap = battleMap;
	}
	
}

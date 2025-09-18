package com.hawk.game.service.tiberium;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hawk.game.service.tiberium.TiberiumConst.TLWGroupType;

public class TLWEliminationGroup {

	public int season;

	public int termId;
	
	/** 组别 S A B*/
	public TLWGroupType groupType;
	
	/** 胜者组*/
	public List<String> winGuildGroup;
	
	/** 败者组*/
	public List<String> lossGuildGroup;
	
	/** 所有参赛联盟*/
	public List<String> guildIds;

	/** 各期对战信息 */
	public Map<Integer, List<TLWBattleData>> battleMap;

	public TLWEliminationGroup() {
		battleMap = new HashMap<>();
		winGuildGroup = new ArrayList<>();
		lossGuildGroup = new ArrayList<>();
		guildIds =  new ArrayList<>();
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
	
	public void setGroupType(TLWGroupType groupType) {
		this.groupType = groupType;
	}
	
	public TLWGroupType getGroupType() {
		return groupType;
	}
	
	
	public Map<Integer, List<TLWBattleData>> getBattleMap() {
		return battleMap;
	}

	public void setBattleMap(Map<Integer, List<TLWBattleData>> battleMap) {
		this.battleMap = battleMap;
	}
	
	public List<String> getGuildIds() {
		return guildIds;
	}
	
	public void setGuildIds(List<String> guildIds) {
		if(guildIds == null){
			return;
		}
		this.guildIds.clear();
		this.guildIds.addAll(guildIds);
	}
	
	public boolean inWinGuildGroup(String guildId){
		return this.winGuildGroup.contains(guildId);
	}
	
	public boolean inLossGuildGroup(String guildId){
		return this.lossGuildGroup.contains(guildId);
	}
	
	public void removeFromWinGuildGroup(String guildId){
		this.winGuildGroup.remove(guildId);
	}
	
	public void removeFromLossGuildGroup(String guildId){
		this.lossGuildGroup.remove(guildId);
	}
	
	public void addToWinGuildGroup(String guildId){
		if(this.winGuildGroup.contains(guildId)){
			return;
		}
		this.winGuildGroup.add(guildId);
	}
	
	public void addToLossGuildGroup(String guildId){
		if(this.lossGuildGroup.contains(guildId)){
			return;
		}
		this.lossGuildGroup.add(guildId);
	}
	
	
	
	public List<String> getLossGuildGroup() {
		return lossGuildGroup;
	}
	
	public List<String> getWinGuildGroup() {
		return winGuildGroup;
	}
	
	public void setWinGuildGroup(List<String> winGuildGroup) {
		if(winGuildGroup == null){
			return;
		}
		this.winGuildGroup.clear();
		this.winGuildGroup.addAll(winGuildGroup);
	}
}

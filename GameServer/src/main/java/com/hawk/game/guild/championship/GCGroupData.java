package com.hawk.game.guild.championship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.annotation.JSONField;
import com.hawk.game.guild.championship.GCConst.GCBattleStage;
import com.hawk.game.guild.championship.GCConst.GCGuildGrade;

public class GCGroupData {
	@JSONField(serialize = false)
	public int termId;
	
	public String id;

	/** 段位*/
	public GCGuildGrade grade;
	
	public String serverId;
	
	/** 当前计算的战斗阶段*/
	private GCBattleStage calcStage = GCBattleStage.TO_8;
	
	/** 当前计算的战斗位置角标*/
	private int calcIndex = 0;
	
	private boolean isCalcFinish;
	
	public List<String> guildIds;
	
	/** 各阶段对战信息*/
	public Map<GCBattleStage, List<GCGuildBattleData>> battleMap;
	
	/** 各阶段积分信息*/
	public Map<GCBattleStage, GCScoreData> scoreDataMap;

	public GCGroupData() {
		guildIds = new ArrayList<>();
		battleMap = new HashMap<>();
		scoreDataMap = new HashMap<>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public GCGuildGrade getGrade() {
		return grade;
	}

	public void setGrade(GCGuildGrade grade) {
		this.grade = grade;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public List<String> getGuildIds() {
		return guildIds;
	}

	public void setGuildIds(List<String> guildIds) {
		this.guildIds = guildIds;
	}
	
	public GCBattleStage getCalcStage() {
		return calcStage;
	}

	public void setCalcStage(GCBattleStage calcStage) {
		this.calcStage = calcStage;
	}

	public int getCalcIndex() {
		return calcIndex;
	}

	public void setCalcIndex(int calcIndex) {
		this.calcIndex = calcIndex;
	}

	public boolean isCalcFinish() {
		return isCalcFinish;
	}

	public void setCalcFinish(boolean isCalcFinish) {
		this.isCalcFinish = isCalcFinish;
	}

	public Map<GCBattleStage, List<GCGuildBattleData>> getBattleMap() {
		return battleMap;
	}

	public void setBattleMap(Map<GCBattleStage, List<GCGuildBattleData>> battleMap) {
		this.battleMap = battleMap;
	}

	public Map<GCBattleStage, GCScoreData> getScoreDataMap() {
		return scoreDataMap;
	}

	public void setScoreDataMap(Map<GCBattleStage, GCScoreData> scoreDataMap) {
		this.scoreDataMap = scoreDataMap;
	}

}

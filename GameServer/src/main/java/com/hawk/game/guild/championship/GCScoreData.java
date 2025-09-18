package com.hawk.game.guild.championship;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 锦标赛玩家信息
 * 
 * @author Jesse
 */
public class GCScoreData {

	/** 是否刷新写入redis */
	public boolean isflushed;

	/** 联盟击杀积分 */
	public Map<String, Long> guildKill;

	/** 个人击杀积分 */
	public Map<String, Long> selfKill;

	/** 个人连胜次数 */
	public Map<String, Long> selfSuccessive;

	/** 个人击败敌人次数 */
	public Map<String, Long> selfBeat;

	public GCScoreData() {
		isflushed = false;
		guildKill = new HashMap<>();
		selfKill = new HashMap<>();
		selfSuccessive = new HashMap<>();
		selfBeat = new HashMap<>();
	}

	public boolean isIsflushed() {
		return isflushed;
	}

	public void setIsflushed(boolean isflushed) {
		this.isflushed = isflushed;
	}

	public Map<String, Long> getGuildKill() {
		return guildKill;
	}

	public void setGuildKill(Map<String, Long> guildKill) {
		this.guildKill = guildKill;
	}

	public Map<String, Long> getSelfKill() {
		return selfKill;
	}

	public void setSelfKill(Map<String, Long> selfKill) {
		this.selfKill = selfKill;
	}

	public Map<String, Long> getSelfSuccessive() {
		return selfSuccessive;
	}

	public void setSelfSuccessive(Map<String, Long> selfSuccessive) {
		this.selfSuccessive = selfSuccessive;
	}

	public Map<String, Long> getSelfBeat() {
		return selfBeat;
	}

	public void setSelfBeat(Map<String, Long> selfBeat) {
		this.selfBeat = selfBeat;
	}

	@JSONField(serialize = false)
	public void addGuildKillScore(String guildId, long addScore) {
		if (addScore > 0) {
			long score = guildKill.getOrDefault(guildId, 0l);
			guildKill.put(guildId, score + addScore);
		}
	}

	@JSONField(serialize = false)
	public void addSelfKillScore(String playerId, long addScore) {
		if (addScore > 0) {
			long score = selfKill.getOrDefault(playerId, 0l);
			selfKill.put(playerId, score + addScore);
		}
	}

	@JSONField(serialize = false)
	public void addSelfBeat(String playerId) {
		long score = selfBeat.getOrDefault(playerId, 0l);
		selfBeat.put(playerId, score + 1);
	}

	@JSONField(serialize = false)
	public void updateSlefSuccessive(String playerId, int successive) {
		if (successive > 1) {
			long score = selfSuccessive.getOrDefault(playerId, 0l);
			selfSuccessive.put(playerId, Math.max(score, successive));
		}
	}
	
	@JSONField(serialize = false)
	public GCScoreData getCopy() {
		GCScoreData scoreData = new GCScoreData();
		scoreData.setIsflushed(false);
		scoreData.setGuildKill(new HashMap<>(this.guildKill));
		scoreData.setSelfKill(new HashMap<>(this.selfKill));
		scoreData.setSelfSuccessive(new HashMap<>(this.selfSuccessive));
		scoreData.setSelfBeat(new HashMap<>(this.selfBeat));
		return scoreData;

	}
	
	/**
	 * 获取联盟杀敌积分变化集合
	 * @param lastScoreData
	 * @return
	 */
	@JSONField(serialize = false)
	public Map<String, Double> getGuildKillChangeMap(GCScoreData lastScoreData) {
		Map<String, Long> lastMap = new HashMap<>();
		if (lastScoreData != null) {
			lastMap = lastScoreData.getGuildKill();
		}
		Map<String, Double> changeMap = new HashMap<>();
		for (Entry<String, Long> entry : this.guildKill.entrySet()) {
			String guildId = entry.getKey();
			long score = entry.getValue();
			// 记录相比上一阶段发生变化的积分
			if(lastMap.containsKey(guildId) && lastMap.get(guildId) == score){
				continue;
			}
			changeMap.put(guildId, (double) score);
		}
		return changeMap;
	}
	
	/**
	 * 获取个人杀敌积分变化集合
	 * @param lastScoreData
	 * @return
	 */
	@JSONField(serialize = false)
	public Map<String, Double> getSelfKillChangeMap(GCScoreData lastScoreData) {
		Map<String, Long> lastMap = new HashMap<>();
		if (lastScoreData != null) {
			lastMap = lastScoreData.getSelfKill();
		}
		Map<String, Double> changeMap = new HashMap<>();
		for (Entry<String, Long> entry : this.selfKill.entrySet()) {
			String playerId = entry.getKey();
			long score = entry.getValue();
			// 记录相比上一阶段发生变化的积分
			if(lastMap.containsKey(playerId) && lastMap.get(playerId) == score){
				continue;
			}
			changeMap.put(playerId, (double) score);
		}
		return changeMap;
	}
	
	/**
	 * 获取个人击败敌人次数变化集合
	 * @param lastScoreData
	 * @return
	 */
	@JSONField(serialize = false)
	public Map<String, Double> getSelfBeatChangeMap(GCScoreData lastScoreData) {
		Map<String, Long> lastMap = new HashMap<>();
		if (lastScoreData != null) {
			lastMap = lastScoreData.getSelfBeat();
		}
		Map<String, Double> changeMap = new HashMap<>();
		for (Entry<String, Long> entry : this.selfBeat.entrySet()) {
			String playerId = entry.getKey();
			long score = entry.getValue();
			// 记录相比上一阶段发生变化的积分
			if (lastMap.containsKey(playerId) && lastMap.get(playerId) == score) {
				continue;
			}
			changeMap.put(playerId, (double) score);
		}
		return changeMap;
	}
	
	/**
	 * 获取个人连胜次数变化集合
	 * @param lastScoreData
	 * @return
	 */
	@JSONField(serialize = false)
	public Map<String, Double> getSelfContinueKillChangeMap(GCScoreData lastScoreData) {
		Map<String, Long> lastMap = new HashMap<>();
		if (lastScoreData != null) {
			lastMap = lastScoreData.getSelfSuccessive();
		}
		Map<String, Double> changeMap = new HashMap<>();
		for (Entry<String, Long> entry : this.selfSuccessive.entrySet()) {
			String playerId = entry.getKey();
			long score = entry.getValue();
			// 记录相比上一阶段有提升的记录
			if (lastMap.containsKey(playerId) && score <= lastMap.get(playerId)) {
				continue;
			}
			changeMap.put(playerId, (double) score);
		}
		return changeMap;
	}

}

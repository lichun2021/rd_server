package com.hawk.activity.type.impl.stronestleader.target.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.PvpBattleEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.type.impl.stronestleader.cfg.ActivityCircularCfg;
import com.hawk.activity.type.impl.stronestleader.entity.ActivityStrongestLeaderEntity;
import com.hawk.activity.type.impl.stronestleader.target.StrongestTargetParser;
import com.hawk.activity.type.impl.stronestleader.target.StrongestTargetType;

public class BreakWarTargetParser implements StrongestTargetParser<PvpBattleEvent> {
	
	@Override
	public StrongestTargetType getTargetType() {
		return StrongestTargetType.BREAK_WAR;
	}
	
	@Override
	public boolean isOverrideScore() {
		return false;
	}

	@Override
	public boolean onEvent(ActivityStrongestLeaderEntity entity, ActivityCircularCfg circularCfg, PvpBattleEvent event) {
		Map<Integer, Integer> armyKillMap = event.getArmyKillMap();
		Map<Integer, Integer> armyHurtMap = event.getArmyHurtMap();
		List<Integer> indexScoreKillList = circularCfg.getIndexScoreListMap().get(0);
		List<Integer> indexScoreHurtList = circularCfg.getIndexScoreListMap().get(1);
		long killScore = getScore(armyKillMap, indexScoreKillList);
		long hurtScore = getScore(armyHurtMap, indexScoreHurtList);
		boolean updata = false;
		long scoreBef = entity.getScore();
		if (killScore > 0) {
			entity.setScore(checkKillLimit(circularCfg, entity.getScore() + killScore));
			updata = true;
		}
		if (hurtScore > 0) {
			entity.setHurtScore(checkHurtLimit(circularCfg, entity.getHurtScore() + hurtScore));
			updata = true;
		}

		logger.info("[strongest] BREAK_WAR target score change, playerId:{}, scoreBef: {}, scoreAft: {}, hurtScore: {}", entity.getPlayerId(), scoreBef, entity.getScore(),
				entity.getHurtScore());
		return updata;
	}
	
	private long checkKillLimit(ActivityCircularCfg circularCfg, long score){
		if(circularCfg.getKillScoreLimit() > 0){
			if(score > circularCfg.getKillScoreLimit()){ //超过积分上限，拦截一下
				score = circularCfg.getKillScoreLimit();
			}
		}
		return score;
	}
	
	private long checkHurtLimit(ActivityCircularCfg circularCfg, long score){
		if(circularCfg.getHurtScoreLimit() > 0){
			if(score > circularCfg.getHurtScoreLimit()){ //超过积分上限，拦截一下
				score = circularCfg.getHurtScoreLimit();
			}
		}
		return score;
	}

	@Override
	public void recordPlayerData(ActivityDataProxy dataGeter, String playerId, ActivityStrongestLeaderEntity entity, PvpBattleEvent event) {
	}

	private long getScore(Map<Integer, Integer> map, List<Integer> scoreList) {
		long score = 0;
		ActivityDataProxy dataGeter = ActivityManager.getInstance().getDataGeter();
		for (Entry<Integer, Integer> entry : map.entrySet()) {
			Integer armyId = entry.getKey();
			Integer killCount = entry.getValue();
			int soldierLevel = dataGeter.getSoldierLevel(armyId);
			if (soldierLevel > scoreList.size()) {
				logger.error("ActivityStrongestLeader BreakWarTargetParser army level error, armyId: {}, soldierLevel: {}, scoreListSize: {}", armyId, soldierLevel,
						scoreList.size());
				continue;
			}
			Integer v = scoreList.get(soldierLevel - 1);
			if (v == null) {
				continue;
			}
			score += 1l * v * killCount;
		}
		return score;
	}
}

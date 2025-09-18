package com.hawk.activity.type.impl.strongestGuild.target.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.log.HawkLog;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.PvpBattleEvent;
import com.hawk.activity.event.speciality.StrongestEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.type.impl.stronestleader.target.StrongestTargetType;
import com.hawk.activity.type.impl.strongestGuild.cfg.StrongestGuildCfg;
import com.hawk.activity.type.impl.strongestGuild.entity.StrongestGuildEntity;
import com.hawk.activity.type.impl.strongestGuild.target.StrongestGuildTargetParser;

/***
 * 王者联盟 喋血枭雄阶段解析器
 * @author yang.rao
 *
 */
public class BreakWarTargetParser implements StrongestGuildTargetParser<PvpBattleEvent> {

	@Override
	public StrongestTargetType getTargetType() {
		return StrongestTargetType.BREAK_WAR;
	}

	@Override
	public boolean isOverrideScore() {
		return false;
	}

	@Override
	public long onEvent(StrongestGuildEntity entity, StrongestGuildCfg circularCfg, PvpBattleEvent event) {
		Map<Integer, Integer> armyKillMap = event.getArmyKillMap();
		Map<Integer, Integer> armyHurtMap = event.getArmyHurtMap();
		List<Integer> indexScoreKillList = circularCfg.getIndexScoreListMap().get(0);
		List<Integer> indexScoreHurtList = circularCfg.getIndexScoreListMap().get(1);
		long killScore = getScore(armyKillMap, indexScoreKillList); // 计算出来的击杀积分
		long hurtScore = getScore(armyHurtMap, indexScoreHurtList); // 计算出来的击伤积分
		long killScoreMax; // 拦截之后的最大积分
		long hurtScoreMax; // 拦截之后的最大积分
		long oldKillScore = entity.getKillScore();
		long oldHurtScore = entity.getHurtScore();
		if (killScore > 0) {
			killScoreMax = checkKillLimit(circularCfg, entity.getKillScore() + killScore);
			if (killScoreMax > entity.getKillScore()) {
				entity.addKillScore(killScoreMax - entity.getKillScore()); // 添加增量
			}
		}
		if (hurtScore > 0) {
			hurtScoreMax = checkHurtLimit(circularCfg, entity.getHurtScore() + hurtScore);
			if (hurtScoreMax > entity.getHurtScore()) {
				entity.addHurtScore(hurtScoreMax - entity.getHurtScore());
			}
		}
		long oldScore = oldKillScore + oldHurtScore;
		long newScore = entity.getKillScore() + entity.getHurtScore();
		entity.setScore(newScore);
		HawkLog.logPrintln(
				"StrongestGuildActivity score add, targetType: {}, playerId: {}, oldScore: {}, newScore: {}, addKillScore: {}, addHurtScore: {}, oldKill: {}, newKill: {}, oldHurt: {}, newHurt: {}",
				getTargetType(), event.getPlayerId(), oldScore, newScore, killScore, hurtScore, oldKillScore, entity.getKillScore(), oldHurtScore, entity.getHurtScore());
		if (newScore > oldScore) {
			return newScore - oldScore;
		} else if (newScore < oldScore) {
			logger.error("breakWar 阶段分数异常, oldHurtScore:{}, oldKillScore:{}, newKillScore:{}, newHurtScore:{}", oldHurtScore, oldKillScore, entity.getKillScore(),
					entity.getHurtScore());
		}
		return 0;
	}

	@Override
	public void recordPlayerData(ActivityDataProxy dataGeter, String playerId, StrongestGuildEntity entity,
			PvpBattleEvent event) {
		
	}
	
	private long checkKillLimit(StrongestGuildCfg circularCfg, long score){
		if(circularCfg.getKillScoreLimit() > 0){
			if(score > circularCfg.getKillScoreLimit()){ //超过积分上限，拦截一下
				score = circularCfg.getKillScoreLimit();
			}
		}
		return score;
	}
	
	private long checkHurtLimit(StrongestGuildCfg circularCfg, long score){
		if(circularCfg.getHurtScoreLimit() > 0){
			if(score > circularCfg.getHurtScoreLimit()){ //超过积分上限，拦截一下
				score = circularCfg.getHurtScoreLimit();
			}
		}
		return score;
	}
	
	private long getScore(Map<Integer, Integer> map, List<Integer> scoreList){
		long score = 0;
		ActivityDataProxy dataGeter = ActivityManager.getInstance().getDataGeter();
		for (Entry<Integer, Integer> entry : map.entrySet()) {
			Integer armyId = entry.getKey();
			Integer killCount = entry.getValue();
			int soldierLevel = dataGeter.getSoldierLevel(armyId);
			if (soldierLevel > scoreList.size()) {
				logger.error("StrongestGuildActivity BreakWarTargetParser army level error, armyId: {}, soldierLevel: {}, scoreListSize: {}", armyId, soldierLevel,
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

	@Override
	public boolean march(StrongestEvent event) {
		return event instanceof PvpBattleEvent;
	}
}

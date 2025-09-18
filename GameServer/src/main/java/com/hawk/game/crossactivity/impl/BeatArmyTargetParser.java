package com.hawk.game.crossactivity.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.tuple.HawkTuple2;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.PvpBattleEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.game.battle.BattleService;
import com.hawk.game.config.CrossConstCfg;
import com.hawk.game.config.CrossIntegralCfg;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossactivity.CrossTargetParser;
import com.hawk.game.crossactivity.CrossTargetType;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.util.GsConst;

public class BeatArmyTargetParser implements CrossTargetParser<PvpBattleEvent> {
	
	@Override
	public CrossTargetType getTargetType() {
		return CrossTargetType.BEAT_ARMY;
	}
	

	@Override
	public boolean calcScore(int termId, CrossIntegralCfg circularCfg, PvpBattleEvent event) {
		// 攻打跨服玩家才有积分
		if (event.isSameServer()) {
			return false;
		}
		String playerId = event.getPlayerId();
		Map<Integer, Integer> killStarAddMap = CrossConstCfg.getInstance().getStarKillMap();
		Map<Integer, Integer> hurtStarAddMap = CrossConstCfg.getInstance().getStarHurtMap();
		Map<Long, Integer> armyKillDetailMap = event.getArmyKillDetailMap();
		Map<Long, Integer> armyHurtDetailMap = event.getArmyHurtDetailMap();
		List<Double> indexScoreKillList = circularCfg.getIndexScoreListMap().get(0);
		List<Double> indexScoreHurtList = circularCfg.getIndexScoreListMap().get(1);
		long killScore = getScore(armyKillDetailMap, indexScoreKillList, killStarAddMap);
		long hurtScore = getScore(armyHurtDetailMap, indexScoreHurtList, hurtStarAddMap);
		long addScore = killScore + hurtScore;

		Player player = GlobalData.getInstance().makesurePlayer(playerId);

		addScore = addScore * circularCfg.getCrossScoreMag() / GsConst.RANDOM_MYRIABIT_BASE;

		// 防守方玩家分数计算
		if (!event.isAtk()) {
			addScore = addScore * circularCfg.getCrossScoreDefense() / GsConst.RANDOM_MYRIABIT_BASE;
		}
		
		int addEff = player.getData().getEffVal(EffType.CROSS_ARMY_KILL_SCORE_ADD);
		addScore = (long) Math.floor(GsConst.EFF_PER * addScore * (GsConst.RANDOM_MYRIABIT_BASE + addEff));
		
		if (event.getMarchType() == WorldMarchType.FORTRESS_SINGLE
				|| event.getMarchType() == WorldMarchType.FORTRESS_MASS
				|| event.getMarchType() == WorldMarchType.FORTRESS_JOIN) {
			addScore += addScore * CrossConstCfg.getInstance().getFortressBattlePointBuff() / GsConst.RANDOM_MYRIABIT_BASE;
		}
		
		CrossActivityService.getInstance().addScore(player, getTargetType(), addScore);
		return true;
	}
	

	private long getScore(Map<Long, Integer> map, List<Double> scoreList ,Map<Integer, Integer> starAddMap) {
		double score = 0;
		ActivityDataProxy dataGeter = ActivityManager.getInstance().getDataGeter();
		for (Entry<Long, Integer> entry : map.entrySet()) {
			Long calcedId = entry.getKey();
			HawkTuple2<Integer, Integer> armyTuple = BattleService.splicArmyId(calcedId);
			int star = armyTuple.first;
			int armyId = armyTuple.second;

			Integer killCount = entry.getValue();
			int soldierLevel = dataGeter.getSoldierLevel(armyId);
			if (soldierLevel > scoreList.size()) {
				logger.error("BeatArmyTargetParser army level error, armyId: {}, soldierLevel: {}, star: {}, scoreListSize: {}", armyId, soldierLevel, star, scoreList.size());
				continue;
			}
			Double v = scoreList.get(soldierLevel - 1);
			if (v == null) {
				continue;
			}
			int addValue = 0;
			Integer starAddValue = starAddMap.get(star);
			if (starAddValue != null) {
				addValue = starAddValue;
			}
			double addScore = 1l * v * killCount * (GsConst.RANDOM_MYRIABIT_BASE + addValue) / GsConst.RANDOM_MYRIABIT_BASE;
			score += Math.floor(addScore);
		}
		return (long)score;
	}
}

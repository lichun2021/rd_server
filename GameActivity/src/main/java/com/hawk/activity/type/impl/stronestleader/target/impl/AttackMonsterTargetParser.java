package com.hawk.activity.type.impl.stronestleader.target.impl;


import com.hawk.activity.event.impl.MonsterAttackEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.type.impl.stronestleader.cfg.ActivityCircularCfg;
import com.hawk.activity.type.impl.stronestleader.entity.ActivityStrongestLeaderEntity;
import com.hawk.activity.type.impl.stronestleader.target.StrongestTargetParser;
import com.hawk.activity.type.impl.stronestleader.target.StrongestTargetType;
import com.hawk.game.protocol.World.MonsterType;

public class AttackMonsterTargetParser implements StrongestTargetParser<MonsterAttackEvent> {
	
	@Override
	public StrongestTargetType getTargetType() {
		return StrongestTargetType.ATTACK_MONSTER;
	}
	
	@Override
	public boolean isOverrideScore() {
		return false;
	}

	@Override
	public boolean onEvent(ActivityStrongestLeaderEntity entity, ActivityCircularCfg circularCfg, MonsterAttackEvent event) {
		int monsterLevel = event.getMonsterLevel();
		int monsterType = event.getMosterType();
		if ((monsterType != MonsterType.TYPE_1_VALUE && monsterType != MonsterType.TYPE_2_VALUE) || !event.isKill()) {
			return false;
		}
		Integer killScore = circularCfg.getIndexScoreList().get(monsterLevel - 1);
		if (killScore == null) {
			return false;
		}
		
		killScore = killScore * event.getAtkTimes();
		long scoreBef = entity.getScore();
		entity.setScore(checkScoreLimit(circularCfg, scoreBef + killScore));
		logger.info("[strongest] ATTACK_MONSTER target score change, playerId: {}, scoreBef: {}, scoreAft: {}", entity.getPlayerId(), scoreBef, entity.getScore());
		return true;
	}

	@Override
	public void recordPlayerData(ActivityDataProxy dataGeter, String playerId, ActivityStrongestLeaderEntity entity, MonsterAttackEvent event) {
	
	}

}

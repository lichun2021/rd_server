package com.hawk.activity.type.impl.stronestleader.target.impl;


import com.hawk.activity.event.impl.TrainSoldierCompleteEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.type.impl.stronestleader.cfg.ActivityCircularCfg;
import com.hawk.activity.type.impl.stronestleader.entity.ActivityStrongestLeaderEntity;
import com.hawk.activity.type.impl.stronestleader.target.StrongestTargetParser;
import com.hawk.activity.type.impl.stronestleader.target.StrongestTargetType;

public class SoliderProgressTargetParser implements StrongestTargetParser<TrainSoldierCompleteEvent> {

	@Override
	public StrongestTargetType getTargetType() {
		return StrongestTargetType.SOLIDER_PROGRESS;
	}

	@Override
	public boolean isOverrideScore() {
		return false;
	}

	@Override
	public boolean onEvent(ActivityStrongestLeaderEntity entity, ActivityCircularCfg circularCfg, TrainSoldierCompleteEvent event) {
		int level = event.getLevel();
		int count = event.getNum();
		Integer killScoree = circularCfg.getIndexScoreList().get(level - 1);
		if (killScoree == null) {
			return false;
		}
		int addScore = killScoree * count;
		long scoreBef = entity.getScore();
		entity.setScore(checkScoreLimit(circularCfg, entity.getScore() + addScore));
		logger.info("[strongest] SOLIDER_PROGRESS target score change, playerId:{}, scoreBef: {}, scoreAft: {}", entity.getPlayerId(), scoreBef, entity.getScore());
		return true;
	}

	@Override
	public void recordPlayerData(ActivityDataProxy dataGeter, String playerId, ActivityStrongestLeaderEntity entity, TrainSoldierCompleteEvent event) {

	}

}

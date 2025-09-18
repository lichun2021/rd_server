package com.hawk.activity.type.impl.stronestleader.target.impl;

import java.util.Map;
import java.util.Map.Entry;

import com.hawk.activity.event.impl.ResourceCollectEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.type.impl.stronestleader.cfg.ActivityCircularCfg;
import com.hawk.activity.type.impl.stronestleader.entity.ActivityStrongestLeaderEntity;
import com.hawk.activity.type.impl.stronestleader.target.StrongestTargetParser;
import com.hawk.activity.type.impl.stronestleader.target.StrongestTargetType;

public class CollectResourceTargetParser implements StrongestTargetParser<ResourceCollectEvent> {
	
	@Override
	public StrongestTargetType getTargetType() {
		return StrongestTargetType.COLLECT_RESOURCE;
	}
	
	@Override
	public boolean isOverrideScore() {
		return false;
	}
	
	@Override
	public boolean onEvent(ActivityStrongestLeaderEntity entity, ActivityCircularCfg circularCfg, ResourceCollectEvent event) {
		Map<Integer, Double> collectMap = event.getCollectMap();
		Map<Integer, Integer> indexScoreMap = circularCfg.getIndexScoreMap();
		long addScore = 0;
		for (Entry<Integer, Double> entry : collectMap.entrySet()) {
			Integer coefficient = indexScoreMap.get(entry.getKey());
			if (coefficient == null) {
				continue;
			}
			addScore += entry.getValue() * coefficient;
		}
		if (addScore <= 0) {
			return false;
		}
		long scoreBef = entity.getScore();
		entity.setScore(checkScoreLimit(circularCfg, entity.getScore() + addScore));
		logger.info("[strongest] COLLECT_RESOURCE target score change, playerId:{}, scoreBef: {}, scoreAft: {}", entity.getPlayerId(), scoreBef, entity.getScore());
		return true;
	}

	@Override
	public void recordPlayerData(ActivityDataProxy dataGeter, String playerId, ActivityStrongestLeaderEntity entity, ResourceCollectEvent event) {
	}
}

package com.hawk.activity.type.impl.strongestGuild.target.impl;

import java.util.Map;
import java.util.Map.Entry;

import com.hawk.activity.event.impl.ResourceCollectEvent;
import com.hawk.activity.event.speciality.StrongestEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.type.impl.stronestleader.target.StrongestTargetType;
import com.hawk.activity.type.impl.strongestGuild.cfg.StrongestGuildCfg;
import com.hawk.activity.type.impl.strongestGuild.entity.StrongestGuildEntity;
import com.hawk.activity.type.impl.strongestGuild.target.StrongestGuildTargetParser;

/***
 * 王者联盟，资源采集阶段解析器
 * @author yang.rao
 *
 */
public class CollectResourceTargetParser implements StrongestGuildTargetParser<ResourceCollectEvent> {

	@Override
	public StrongestTargetType getTargetType() {
		return StrongestTargetType.COLLECT_RESOURCE;
	}

	@Override
	public boolean isOverrideScore() {
		return false;
	}

	@Override
	public long onEvent(StrongestGuildEntity entity, StrongestGuildCfg circularCfg, ResourceCollectEvent event) {
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
			return 0;
		}
		long scoreBef = entity.getScore();
		long nowScore = checkScoreLimit(circularCfg, entity.getScore() + addScore);
		long actualAdd = nowScore - scoreBef;
		entity.setScore(nowScore);
		return actualAdd;
	}

	@Override
	public void recordPlayerData(ActivityDataProxy dataGeter, String playerId, StrongestGuildEntity entity,
			ResourceCollectEvent event) {
		
	}

	@Override
	public boolean march(StrongestEvent event) {
		return event instanceof ResourceCollectEvent;
	}
}

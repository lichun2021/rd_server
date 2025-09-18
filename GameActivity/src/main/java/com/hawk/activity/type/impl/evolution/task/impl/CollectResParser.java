package com.hawk.activity.type.impl.evolution.task.impl;


import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.hawk.activity.event.impl.ResourceCollectEvent;
import com.hawk.activity.type.impl.evolution.cfg.EvolutionTaskCfg;
import com.hawk.activity.type.impl.evolution.entity.ActivityEvolutionEntity;
import com.hawk.activity.type.impl.evolution.entity.TaskItem;
import com.hawk.activity.type.impl.evolution.task.EvolutionTaskParser;
import com.hawk.activity.type.impl.evolution.task.EvolutionTaskType;

public class CollectResParser implements EvolutionTaskParser<ResourceCollectEvent> {

	@Override
	public EvolutionTaskType getTaskType() {
		return EvolutionTaskType.RES_COLLECT;
	}

	@Override
	public boolean onEventUpdate(ActivityEvolutionEntity dataEntity, EvolutionTaskCfg cfg, TaskItem taskItem, ResourceCollectEvent event) {
		long addNum = 0;
		Map<Integer, Double> collectMap = event.getCollectMap();
		List<Integer> list = cfg.getConditionList();
		if (list.isEmpty() || list.get(0) == 0) {
			for (Entry<Integer, Double> collectEntry : collectMap.entrySet()) {
				addNum += collectEntry.getValue() * event.getResWeight(collectEntry.getKey());
			}
		} else {
			int type = list.get(0);
			for (Entry<Integer, Double> collectEntry : collectMap.entrySet()) {
				if (collectEntry.getKey() == type) {
					addNum += collectEntry.getValue() * event.getResWeight(collectEntry.getKey());
				}
			}
		}
		
		if (addNum <= 0) {
			return false;
		}
		
		return onAddValue(dataEntity, cfg, taskItem, addNum);
	}
}

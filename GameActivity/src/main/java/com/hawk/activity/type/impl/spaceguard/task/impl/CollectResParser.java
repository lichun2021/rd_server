package com.hawk.activity.type.impl.spaceguard.task.impl;


import java.util.Map;
import java.util.Map.Entry;
import com.hawk.activity.event.impl.ResourceCollectEvent;
import com.hawk.activity.type.impl.spaceguard.cfg.SpaceGuardPointCfg;
import com.hawk.activity.type.impl.spaceguard.entity.PointTaskItem;
import com.hawk.activity.type.impl.spaceguard.entity.SpaceGuardEntity;
import com.hawk.activity.type.impl.spaceguard.task.SpaceMechaTaskParser;
import com.hawk.activity.type.impl.spaceguard.task.SpaceMechaTaskType;

public class CollectResParser implements SpaceMechaTaskParser<ResourceCollectEvent> {

	@Override
	public SpaceMechaTaskType getTaskType() {
		return SpaceMechaTaskType.RESOURCE_COLLECT;
	}

	@Override
	public boolean onEventUpdate(SpaceGuardEntity dataEntity, SpaceGuardPointCfg cfg, PointTaskItem taskItem, ResourceCollectEvent event) {
		long addNum = 0;
		Map<Integer, Double> collectMap = event.getCollectMap();
		if (cfg.getConditionList().get(0) == 0) {
			for (Entry<Integer, Double> collectEntry : collectMap.entrySet()) {
				addNum += collectEntry.getValue() * event.getResWeight(collectEntry.getKey());
			}
		} else {
			int type = cfg.getConditionList().get(0);
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

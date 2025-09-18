package com.hawk.activity.type.impl.order.task.impl;


import java.util.Map;
import java.util.Map.Entry;

import com.hawk.activity.event.impl.ResourceCollectEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class CollectResParser implements OrderTaskParser<ResourceCollectEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.RESOURCE_COLLECT;
	}

	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, ResourceCollectEvent event) {
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
		return onAddValue(dataEntity, cfg, orderItem, addNum);
	}
}

package com.hawk.activity.type.impl.order.task.impl;


import com.hawk.activity.event.impl.OccupyPresidentEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class OccupyPresidentFinallyParser implements OrderTaskParser<OccupyPresidentEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.OCCUPY_PRESIDENT_FINALLY;
	}

	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, OccupyPresidentEvent event) {
		return onAddValue(dataEntity, cfg, orderItem, 1);
	}

	

}

package com.hawk.activity.type.impl.order.task.impl;


import com.hawk.activity.event.impl.EnterPresidentEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class EnterPresidentParser implements OrderTaskParser<EnterPresidentEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.ENTER_PRESIDENT;
	}


	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, EnterPresidentEvent event) {
		return onAddValue(dataEntity, cfg, orderItem, 1);
	}

	

}

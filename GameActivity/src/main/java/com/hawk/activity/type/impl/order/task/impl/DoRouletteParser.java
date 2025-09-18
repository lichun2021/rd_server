package com.hawk.activity.type.impl.order.task.impl;


import com.hawk.activity.event.impl.DoRouletteEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class DoRouletteParser implements OrderTaskParser<DoRouletteEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.DO_ROULETTE;
	}


	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, DoRouletteEvent event) {
		return onAddValue(dataEntity, cfg, orderItem, event.getCount());
	}

	

}

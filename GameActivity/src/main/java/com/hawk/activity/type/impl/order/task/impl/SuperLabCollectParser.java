package com.hawk.activity.type.impl.order.task.impl;


import com.hawk.activity.event.impl.PowerLabItemDropEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class SuperLabCollectParser implements OrderTaskParser<PowerLabItemDropEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.SUPER_LAB_COLLECT;
	}


	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, PowerLabItemDropEvent event) {
		return onAddValue(dataEntity, cfg, orderItem, event.getCount());
	}

	

}

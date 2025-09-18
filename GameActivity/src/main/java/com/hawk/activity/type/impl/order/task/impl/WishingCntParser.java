package com.hawk.activity.type.impl.order.task.impl;


import com.hawk.activity.event.impl.WishingEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class WishingCntParser implements OrderTaskParser<WishingEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.WISHING_TIMES;
	}


	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, WishingEvent event) {
		return onAddValue(dataEntity, cfg, orderItem, 1);
	}

	

}

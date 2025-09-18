package com.hawk.activity.type.impl.order.task.impl;


import com.hawk.activity.event.impl.BuyMonthCardEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class BuyMonthCardParser implements OrderTaskParser<BuyMonthCardEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.BUY_MONTH_CARD;
	}


	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, BuyMonthCardEvent event) {
		return onAddValue(dataEntity, cfg, orderItem, 1);
	}

	

}

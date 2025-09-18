package com.hawk.activity.type.impl.order.task.impl;


import com.hawk.activity.event.impl.LmjyGetRewardEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class LmjyGetRewardParser implements OrderTaskParser<LmjyGetRewardEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.LMJY_GET_AWARD;
	}


	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, LmjyGetRewardEvent event) {
		return onAddValue(dataEntity, cfg, orderItem, 1);
	}

	

}

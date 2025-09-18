package com.hawk.activity.type.impl.order.task.impl;

import com.hawk.activity.event.impl.HonorItemDropEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;
//TODO
public class HonorCollectParser implements OrderTaskParser<HonorItemDropEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.HONOR_COLLECT;
	}


	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, HonorItemDropEvent event) {
		return onAddValue(dataEntity, cfg, orderItem, event.getCount());
	}

	

}

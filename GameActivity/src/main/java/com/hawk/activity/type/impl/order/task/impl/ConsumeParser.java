package com.hawk.activity.type.impl.order.task.impl;


import com.hawk.activity.event.impl.ConsumeMoneyEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class ConsumeParser implements OrderTaskParser<ConsumeMoneyEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.GOLD_CONSUME;
	}


	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, ConsumeMoneyEvent event) {
		if (cfg.getConditionList().get(0) != 0 && !cfg.getConditionList().contains(event.getResType())) {
			return false;
		}
	
		return onAddValue(dataEntity, cfg, orderItem, event.getNum());
	}

	

}

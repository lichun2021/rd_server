package com.hawk.activity.type.impl.order.task.impl;


import com.hawk.activity.event.impl.RandomHeroEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class GachaParser implements OrderTaskParser<RandomHeroEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.GACHA;
	}


	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, RandomHeroEvent event) {
		if (cfg.getConditionList().get(0) != 0 && !cfg.getConditionList().contains(event.getGachaType())) {
			return false;
		}
	
		return onAddValue(dataEntity, cfg, orderItem, event.getCount());
	}

	

}

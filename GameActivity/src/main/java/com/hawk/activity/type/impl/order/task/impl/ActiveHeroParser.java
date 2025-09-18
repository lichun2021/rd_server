package com.hawk.activity.type.impl.order.task.impl;


import com.hawk.activity.event.impl.HeroUnlockEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class ActiveHeroParser implements OrderTaskParser<HeroUnlockEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.ACTIVE_HERO;
	}

	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, HeroUnlockEvent event) {
		if (cfg.getConditionList().get(0) != 0 && !cfg.getConditionList().contains(event.getQuality())) {
			return false;
		}
		return onAddValue(dataEntity, cfg, orderItem, 1);
	}

}

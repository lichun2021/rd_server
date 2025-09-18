package com.hawk.activity.type.impl.order.task.impl;


import com.hawk.activity.event.impl.TechnologyLevelUpEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class TechLvlPowerParser implements OrderTaskParser<TechnologyLevelUpEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.TECH_POWER_UP;
	}

	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, TechnologyLevelUpEvent event) {
		return onAddValue(dataEntity, cfg, orderItem, event.getAddPowar());
	}

}

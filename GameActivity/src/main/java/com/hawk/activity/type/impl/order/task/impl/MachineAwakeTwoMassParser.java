package com.hawk.activity.type.impl.order.task.impl;


import com.hawk.activity.event.impl.MachineAwakeTwoEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class MachineAwakeTwoMassParser implements OrderTaskParser<MachineAwakeTwoEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.MACHINE_AWAKE_TWO_MASS;
	}


	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, MachineAwakeTwoEvent event) {
		if (event.isMass()) {
			return onAddValue(dataEntity, cfg, orderItem, 1);
		}
		return false;
	}

	

}

package com.hawk.activity.type.impl.order.task.impl;

import com.hawk.activity.event.impl.MachineAwakeTwoEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class MachineAwakeTwoKillParser implements OrderTaskParser<MachineAwakeTwoEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.MACHINE_AWAKE_TWO_KILL;
	}

	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, MachineAwakeTwoEvent event) {
		if (event.isKill() || event.isFinalKill()) {
			return onAddValue(dataEntity, cfg, orderItem, 1);
		}
		return false;
	}

}

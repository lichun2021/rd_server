package com.hawk.activity.type.impl.order.task.impl;


import com.hawk.activity.event.impl.OccupySuperWeaponEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class OccupySuperWeaponFinallyParser implements OrderTaskParser<OccupySuperWeaponEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.OCCUPY_SUPER_WEAPON_FINALLY;
	}


	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, OccupySuperWeaponEvent event) {
		if (event.isFinally()) {
			return onAddValue(dataEntity, cfg, orderItem, 1);
		}
		return false;
	}

}

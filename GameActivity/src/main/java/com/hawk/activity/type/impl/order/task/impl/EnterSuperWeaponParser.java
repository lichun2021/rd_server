package com.hawk.activity.type.impl.order.task.impl;


import com.hawk.activity.event.impl.EnterSuperWeaponEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class EnterSuperWeaponParser implements OrderTaskParser<EnterSuperWeaponEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.ENTER_SUPER_WEAPON;
	}


	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, EnterSuperWeaponEvent event) {
		return onAddValue(dataEntity, cfg, orderItem, 1);
	}

	

}

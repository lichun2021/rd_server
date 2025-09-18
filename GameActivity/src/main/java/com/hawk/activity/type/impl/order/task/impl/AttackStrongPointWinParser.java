package com.hawk.activity.type.impl.order.task.impl;


import com.hawk.activity.event.impl.OccupyStrongpointEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class AttackStrongPointWinParser implements OrderTaskParser<OccupyStrongpointEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.ATTACK_STRONGPOINT_WIN;
	}


	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, OccupyStrongpointEvent event) {
		if (cfg.getConditionList().get(0) != 0 && !cfg.getConditionList().contains(event.getPointLvl())) {
			return false;
		}
		
		if (event.isAtkWin()) {
			return onAddValue(dataEntity, cfg, orderItem, 1);
		}
		return false;
	}

	

}

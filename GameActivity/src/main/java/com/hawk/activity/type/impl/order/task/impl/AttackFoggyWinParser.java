package com.hawk.activity.type.impl.order.task.impl;


import com.hawk.activity.event.impl.AttackFoggyEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class AttackFoggyWinParser implements OrderTaskParser<AttackFoggyEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.ATTACK_FOGGY_WIN;
	}


	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, AttackFoggyEvent event) {
		if(event.isWin()){
			return onAddValue(dataEntity, cfg, orderItem, 1);
		}
		return false;
	}

	

}

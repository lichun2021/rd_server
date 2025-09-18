package com.hawk.activity.type.impl.order.task.impl;


import com.hawk.activity.event.impl.PvpBattleEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class AttackCityCrossParser implements OrderTaskParser<PvpBattleEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.ATK_CITY_CROSS;
	}


	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, PvpBattleEvent event) {
		if(event.isAtk() && !event.isSameServer() && event.isInCity()){
			return onAddValue(dataEntity, cfg, orderItem, 1);
		}
		return false;
	}

	

}

package com.hawk.activity.type.impl.order.task.impl;


import com.hawk.activity.event.impl.ActiveSkinEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;
import com.hawk.game.protocol.Dress.DressType;

public class ActiveForeverSkinParser implements OrderTaskParser<ActiveSkinEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.ACTIVE_FOREVER_SKIN;
	}


	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, ActiveSkinEvent event) {
		if(event.getContinueSeconds() == 0 && event.getDressType() == DressType.DERMA_VALUE){
			return onAddValue(dataEntity, cfg, orderItem, 1);
		}
		return false;
	}

	

}

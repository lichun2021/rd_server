package com.hawk.activity.type.impl.order.task.impl;


import com.hawk.activity.event.impl.OpenPandoraBoxEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class OpenPandoraBoxParser implements OrderTaskParser<OpenPandoraBoxEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.OPEN_PANDORA_BOX;
	}


	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, OpenPandoraBoxEvent event) {
		return onAddValue(dataEntity, cfg, orderItem, event.getCount());
	}

	

}

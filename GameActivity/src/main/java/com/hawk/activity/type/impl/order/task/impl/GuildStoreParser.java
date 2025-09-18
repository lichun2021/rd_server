package com.hawk.activity.type.impl.order.task.impl;

import com.hawk.activity.event.impl.GuildStoreEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class GuildStoreParser implements OrderTaskParser<GuildStoreEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.GUILD_STORE;
	}

	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, GuildStoreEvent event) {
		return onAddValue(dataEntity, cfg, orderItem, 1);
	}

}

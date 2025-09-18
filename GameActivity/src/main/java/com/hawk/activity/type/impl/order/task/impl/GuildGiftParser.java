package com.hawk.activity.type.impl.order.task.impl;

import com.hawk.activity.event.impl.GuildGiftEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class GuildGiftParser implements OrderTaskParser<GuildGiftEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.GUILD_GIFT;
	}

	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, GuildGiftEvent event) {
		return onAddValue(dataEntity, cfg, orderItem, event.getCount());
	}

}

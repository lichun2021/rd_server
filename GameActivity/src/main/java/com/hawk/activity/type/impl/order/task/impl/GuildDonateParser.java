package com.hawk.activity.type.impl.order.task.impl;

import com.hawk.activity.event.impl.GuildDonateEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class GuildDonateParser implements OrderTaskParser<GuildDonateEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.GUILD_DONATE;
	}

	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, GuildDonateEvent event) {
		return onAddValue(dataEntity, cfg, orderItem, 1);
	}

}

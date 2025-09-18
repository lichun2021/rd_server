package com.hawk.activity.type.impl.order.task.impl;

import com.hawk.activity.event.impl.GuildHelpEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class GuildHelpParser implements OrderTaskParser<GuildHelpEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.GUILD_HELP;
	}

	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, GuildHelpEvent event) {
		return onAddValue(dataEntity, cfg, orderItem, 1);
	}

}

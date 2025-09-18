package com.hawk.activity.type.impl.order.task.impl;


import com.hawk.activity.event.impl.CrossScoreEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class CSGuildScoreParser implements OrderTaskParser<CrossScoreEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.CS_GUILD_SCORE;
	}


	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, CrossScoreEvent event) {
		if (event.getGuildScore() >= cfg.getConditionList().get(0)) {
			return onAddValue(dataEntity, cfg, orderItem, 1);
		}
		return false;
	}

}

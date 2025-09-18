package com.hawk.activity.type.impl.order.task.impl;


import com.hawk.activity.event.impl.CWScoreEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class CWScoreParser implements OrderTaskParser<CWScoreEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.CW_SCORE;
	}


	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, CWScoreEvent event) {
		if (event.getScore() >= cfg.getConditionList().get(0)) {
			return onAddValue(dataEntity, cfg, orderItem, 1);
		}
		return false;
	}

	

}

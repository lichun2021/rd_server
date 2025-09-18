package com.hawk.activity.type.impl.order.task.impl;


import com.hawk.activity.event.impl.YuriRevengeRewardEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class YuriRevengeRewarParser implements OrderTaskParser<YuriRevengeRewardEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.YURI_REVENGE_REWARD;
	}

	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, YuriRevengeRewardEvent event) {
		if (event.getRewardId() < cfg.getConditionList().get(0)) {
			return false;
		}

		return onAddValue(dataEntity, cfg, orderItem, 1);
	}
}

package com.hawk.activity.type.impl.order.task.impl;


import com.hawk.activity.event.impl.TrainSoldierCompleteEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class TrainArmyParser implements OrderTaskParser<TrainSoldierCompleteEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.TRAIN_SOLDIER_COMPLETE_NUM;
	}

	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, TrainSoldierCompleteEvent event) {
		if (cfg.getConditionList().get(0) != 0 && !cfg.getConditionList().contains(event.getTrainId())) {
			return false;
		}

		return onAddValue(dataEntity, cfg, orderItem, event.getNum());
	}
}

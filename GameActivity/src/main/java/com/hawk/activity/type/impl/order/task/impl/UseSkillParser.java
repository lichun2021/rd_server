package com.hawk.activity.type.impl.order.task.impl;


import com.hawk.activity.event.impl.CastSkillEvent;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.order.task.OrderTaskType;

public class UseSkillParser implements OrderTaskParser<CastSkillEvent> {

	@Override
	public OrderTaskType getTaskType() {
		return OrderTaskType.USE_SKILL;
	}


	@Override
	public boolean onEventUpdate(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, CastSkillEvent event) {
		if (cfg.getConditionList().get(0) != 0 && !cfg.getConditionList().contains(event.getSkillId())) {
			return false;
		}
		return onAddValue(dataEntity, cfg, orderItem, 1);
	}

	

}

package com.hawk.activity.type.impl.evolution.task.impl;

import com.hawk.activity.event.impl.RechargeMoneyEvent;
import com.hawk.activity.type.impl.evolution.cfg.EvolutionTaskCfg;
import com.hawk.activity.type.impl.evolution.entity.ActivityEvolutionEntity;
import com.hawk.activity.type.impl.evolution.entity.TaskItem;
import com.hawk.activity.type.impl.evolution.task.EvolutionTaskParser;
import com.hawk.activity.type.impl.evolution.task.EvolutionTaskType;

public class RechargeMoneyParser implements EvolutionTaskParser<RechargeMoneyEvent> {

	@Override
	public EvolutionTaskType getTaskType() {
		return EvolutionTaskType.RECHARGE_COUNT;
	}
	
	@Override
	public boolean onEventUpdate(ActivityEvolutionEntity dataEntity, EvolutionTaskCfg cfg, TaskItem taskItem, RechargeMoneyEvent event) {
		return onAddValue(dataEntity, cfg, taskItem, event.getMoney());
	}
	
}

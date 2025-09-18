package com.hawk.activity.type.impl.evolution.task.impl;

import com.hawk.activity.event.impl.EvolutionShareEvent;
import com.hawk.activity.type.impl.evolution.cfg.EvolutionTaskCfg;
import com.hawk.activity.type.impl.evolution.entity.ActivityEvolutionEntity;
import com.hawk.activity.type.impl.evolution.entity.TaskItem;
import com.hawk.activity.type.impl.evolution.task.EvolutionTaskParser;
import com.hawk.activity.type.impl.evolution.task.EvolutionTaskType;

public class EvolutionShareParser implements EvolutionTaskParser<EvolutionShareEvent> {

	@Override
	public EvolutionTaskType getTaskType() {
		return EvolutionTaskType.SHARE_NUM;
	}
	
	@Override
	public boolean onEventUpdate(ActivityEvolutionEntity dataEntity, EvolutionTaskCfg cfg, TaskItem taskItem, EvolutionShareEvent event) {
		return onAddValue(dataEntity, cfg, taskItem, 1);
	}
	
}

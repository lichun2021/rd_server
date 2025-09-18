package com.hawk.activity.type.impl.evolution.task.impl;


import java.util.List;

import com.hawk.activity.event.impl.TrainSoldierCompleteEvent;
import com.hawk.activity.type.impl.evolution.cfg.EvolutionTaskCfg;
import com.hawk.activity.type.impl.evolution.entity.ActivityEvolutionEntity;
import com.hawk.activity.type.impl.evolution.entity.TaskItem;
import com.hawk.activity.type.impl.evolution.task.EvolutionTaskParser;
import com.hawk.activity.type.impl.evolution.task.EvolutionTaskType;

public class TrainArmyParser implements EvolutionTaskParser<TrainSoldierCompleteEvent> {

	@Override
	public EvolutionTaskType getTaskType() {
		return EvolutionTaskType.TRAIN_SOLDIER_COMPLETE_NUM;
	}

	@Override
	public boolean onEventUpdate(ActivityEvolutionEntity dataEntity, EvolutionTaskCfg cfg, TaskItem taskItem, TrainSoldierCompleteEvent event) {
		List<Integer> list = cfg.getConditionList();
		if (!list.isEmpty() && list.get(0) != 0 && !list.contains(event.getTrainId())) {
			return false;
		}

		return onAddValue(dataEntity, cfg, taskItem, event.getNum());
	}
}

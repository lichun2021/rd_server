package com.hawk.activity.type.impl.evolution.task.impl;


import java.util.List;

import com.hawk.activity.event.impl.MonsterAttackEvent;
import com.hawk.activity.type.impl.evolution.cfg.EvolutionTaskCfg;
import com.hawk.activity.type.impl.evolution.entity.ActivityEvolutionEntity;
import com.hawk.activity.type.impl.evolution.entity.TaskItem;
import com.hawk.activity.type.impl.evolution.task.EvolutionTaskParser;
import com.hawk.activity.type.impl.evolution.task.EvolutionTaskType;

public class AttackMonsterParser implements EvolutionTaskParser<MonsterAttackEvent> {

	@Override
	public EvolutionTaskType getTaskType() {
		return EvolutionTaskType.KILL_OLD_MONSTER_LEVEL;
	}

	@Override
	public boolean onEventUpdate(ActivityEvolutionEntity dataEntity, EvolutionTaskCfg cfg, TaskItem taskItem, MonsterAttackEvent event) {
		int lvl = event.getMonsterLevel();
		List<Integer> conditionList = cfg.getConditionList();
		
		if (!conditionList.isEmpty() && conditionList.get(0) != 0 && (conditionList.size() != 2 || lvl < conditionList.get(0) || lvl > conditionList.get(1))) {
			return false;
		}

		if (event.isKill()) {
			return onAddValue(dataEntity, cfg, taskItem, event.getAtkTimes());
		}
		
		return false;
	}

}

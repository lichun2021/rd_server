package com.hawk.activity.type.impl.spaceguard.task.impl;

import java.util.List;
import com.hawk.activity.event.impl.MonsterAttackEvent;
import com.hawk.activity.type.impl.spaceguard.cfg.SpaceGuardPointCfg;
import com.hawk.activity.type.impl.spaceguard.entity.PointTaskItem;
import com.hawk.activity.type.impl.spaceguard.entity.SpaceGuardEntity;
import com.hawk.activity.type.impl.spaceguard.task.SpaceMechaTaskParser;
import com.hawk.activity.type.impl.spaceguard.task.SpaceMechaTaskType;

public class AttackMonsterParser implements SpaceMechaTaskParser<MonsterAttackEvent> {

	@Override
	public SpaceMechaTaskType getTaskType() {
		return SpaceMechaTaskType.KILL_OLD_MONSTER_LEVEL;
	}

	@Override
	public boolean onEventUpdate(SpaceGuardEntity dataEntity, SpaceGuardPointCfg cfg, PointTaskItem taskItem,
			MonsterAttackEvent event) {
		int lvl = event.getMonsterLevel();
		List<Integer> conditionList = cfg.getConditionList();
		if (conditionList.size() != 2 || (lvl < conditionList.get(0) || lvl > conditionList.get(1))) {
			return false;
		}

		if (event.isKill()) {
			return onAddValue(dataEntity, cfg, taskItem, event.getAtkTimes());
		}
		return false;
	}
	
}

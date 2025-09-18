package com.hawk.activity.type.impl.spaceguard.task.impl;

import com.hawk.activity.event.impl.AttackFoggyEvent;
import com.hawk.activity.type.impl.spaceguard.cfg.SpaceGuardPointCfg;
import com.hawk.activity.type.impl.spaceguard.entity.PointTaskItem;
import com.hawk.activity.type.impl.spaceguard.entity.SpaceGuardEntity;
import com.hawk.activity.type.impl.spaceguard.task.SpaceMechaTaskParser;
import com.hawk.activity.type.impl.spaceguard.task.SpaceMechaTaskType;

public class AttackFoggyWinParser implements SpaceMechaTaskParser<AttackFoggyEvent> {

	@Override
	public SpaceMechaTaskType getTaskType() {
		return SpaceMechaTaskType.ATTACK_FOGGY_WIN;
	}

	@Override
	public boolean onEventUpdate(SpaceGuardEntity dataEntity, SpaceGuardPointCfg cfg, PointTaskItem taskItem,
			AttackFoggyEvent event) {
		if(event.isWin()){
			return onAddValue(dataEntity, cfg, taskItem, 1);
		}
		return false;
	}

}

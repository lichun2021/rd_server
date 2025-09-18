package com.hawk.activity.type.impl.spaceguard.task.impl;


import com.hawk.activity.event.impl.AddTavernScoreEvent;
import com.hawk.activity.type.impl.spaceguard.cfg.SpaceGuardPointCfg;
import com.hawk.activity.type.impl.spaceguard.entity.PointTaskItem;
import com.hawk.activity.type.impl.spaceguard.entity.SpaceGuardEntity;
import com.hawk.activity.type.impl.spaceguard.task.SpaceMechaTaskParser;
import com.hawk.activity.type.impl.spaceguard.task.SpaceMechaTaskType;

public class DailyActiveScoreParser implements SpaceMechaTaskParser<AddTavernScoreEvent> {

	@Override
	public SpaceMechaTaskType getTaskType() {
		return SpaceMechaTaskType.DAILY_ACTIVE_SCORE;
	}

	@Override
	public boolean onEventUpdate(SpaceGuardEntity dataEntity, SpaceGuardPointCfg cfg, PointTaskItem taskItem, AddTavernScoreEvent event) {
		return onAddValue(dataEntity, cfg, taskItem, event.getScore());
	}

}

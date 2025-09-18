package com.hawk.activity.type.impl.spaceguard.task.impl;

import com.hawk.activity.event.impl.SpaceMechaDailyLoginEvent;
import com.hawk.activity.type.impl.spaceguard.cfg.SpaceGuardPointCfg;
import com.hawk.activity.type.impl.spaceguard.entity.PointTaskItem;
import com.hawk.activity.type.impl.spaceguard.entity.SpaceGuardEntity;
import com.hawk.activity.type.impl.spaceguard.task.SpaceMechaTaskParser;
import com.hawk.activity.type.impl.spaceguard.task.SpaceMechaTaskType;

public class DailyLoginParser implements SpaceMechaTaskParser<SpaceMechaDailyLoginEvent> {

	@Override
	public SpaceMechaTaskType getTaskType() {
		return SpaceMechaTaskType.DAILY_LOGIN;
	}

	@Override
	public boolean onEventUpdate(SpaceGuardEntity dataEntity, SpaceGuardPointCfg cfg, PointTaskItem taskItem, SpaceMechaDailyLoginEvent event) {
		return onAddValue(dataEntity, cfg, taskItem, 1);
	}

}

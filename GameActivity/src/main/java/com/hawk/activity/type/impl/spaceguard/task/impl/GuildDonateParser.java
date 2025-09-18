package com.hawk.activity.type.impl.spaceguard.task.impl;


import com.hawk.activity.event.impl.GuildDonateEvent;
import com.hawk.activity.type.impl.spaceguard.cfg.SpaceGuardPointCfg;
import com.hawk.activity.type.impl.spaceguard.entity.PointTaskItem;
import com.hawk.activity.type.impl.spaceguard.entity.SpaceGuardEntity;
import com.hawk.activity.type.impl.spaceguard.task.SpaceMechaTaskParser;
import com.hawk.activity.type.impl.spaceguard.task.SpaceMechaTaskType;

public class GuildDonateParser implements SpaceMechaTaskParser<GuildDonateEvent> {

	@Override
	public SpaceMechaTaskType getTaskType() {
		return SpaceMechaTaskType.GUILD_DONATE;
	}

	@Override
	public boolean onEventUpdate(SpaceGuardEntity dataEntity, SpaceGuardPointCfg cfg, PointTaskItem taskItem, GuildDonateEvent event) {
		return onAddValue(dataEntity, cfg, taskItem, 1);
	}

}

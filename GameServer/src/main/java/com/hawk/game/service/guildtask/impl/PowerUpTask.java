package com.hawk.game.service.guildtask.impl;

import com.hawk.game.service.guildtask.GuildTask;
import com.hawk.game.service.guildtask.GuildTaskCfgItem;
import com.hawk.game.service.guildtask.GuildTaskEvent;
import com.hawk.game.service.guildtask.GuildTaskItem;
import com.hawk.game.service.guildtask.GuildTaskType;
import com.hawk.game.service.guildtask.event.PowerUpTaskEvent;

/**
 * 战力增长任务
 * 
 * @author jesse
 *
 */
@GuildTask(taskType = GuildTaskType.battle_point_increase)
public class PowerUpTask implements IGuildTask {

	@Override
	public <T extends GuildTaskEvent> boolean refreshTask(String guildId, T taskEvent, GuildTaskItem taskItem, GuildTaskCfgItem cfg) {
		PowerUpTaskEvent event = (PowerUpTaskEvent) taskEvent;
		int value = Math.min(taskItem.getValue() + event.getPowerUp(), cfg.getValue());
		taskItem.setValue(value);
		return true;
	}

}

package com.hawk.game.service.guildtask.impl;

import com.hawk.game.service.guildtask.GuildTask;
import com.hawk.game.service.guildtask.GuildTaskCfgItem;
import com.hawk.game.service.guildtask.GuildTaskEvent;
import com.hawk.game.service.guildtask.GuildTaskItem;
import com.hawk.game.service.guildtask.GuildTaskType;
import com.hawk.game.service.guildtask.event.KillMonsterTaskEvent;

/**
 * 联盟成员完成野怪击杀任务
 * 
 * @author jesse
 *
 */
@GuildTask(taskType = GuildTaskType.kill_monster)
public class KillMonsterTask implements IGuildTask {

	@Override
	public <T extends GuildTaskEvent> boolean refreshTask(String guildId, T taskEvent, GuildTaskItem taskItem, GuildTaskCfgItem cfg) {
		KillMonsterTaskEvent event = (KillMonsterTaskEvent) taskEvent;
		int value = Math.min(taskItem.getValue() + event.getAtkTimes(), cfg.getValue());
		taskItem.setValue(value);
		return true;
	}

}

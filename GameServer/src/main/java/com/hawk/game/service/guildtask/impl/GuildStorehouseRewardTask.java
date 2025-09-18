package com.hawk.game.service.guildtask.impl;

import com.hawk.game.service.guildtask.GuildTask;
import com.hawk.game.service.guildtask.GuildTaskCfgItem;
import com.hawk.game.service.guildtask.GuildTaskEvent;
import com.hawk.game.service.guildtask.GuildTaskItem;
import com.hawk.game.service.guildtask.GuildTaskType;

/**
 * 联盟成员领取联盟宝藏次数任务
 * 
 * @author jesse
 *
 */
@GuildTask(taskType = GuildTaskType.guild_storehouse)
public class GuildStorehouseRewardTask implements IGuildTask {

	@Override
	public <T extends GuildTaskEvent> boolean refreshTask(String guildId, T taskEvent, GuildTaskItem taskItem, GuildTaskCfgItem cfg) {
		int value = Math.min(taskItem.getValue() + 1, cfg.getValue());
		taskItem.setValue(value);
		return true;
	}

}

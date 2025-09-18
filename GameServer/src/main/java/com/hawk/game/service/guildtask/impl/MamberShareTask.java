package com.hawk.game.service.guildtask.impl;

import com.hawk.game.global.LocalRedis;
import com.hawk.game.service.guildtask.GuildTask;
import com.hawk.game.service.guildtask.GuildTaskCfgItem;
import com.hawk.game.service.guildtask.GuildTaskEvent;
import com.hawk.game.service.guildtask.GuildTaskItem;
import com.hawk.game.service.guildtask.GuildTaskType;

/**
 * 联盟成员分享任务
 * 
 * @author jesse
 *
 */
@GuildTask(taskType = GuildTaskType.guild_share)
public class MamberShareTask implements IGuildTask {

	@Override
	public <T extends GuildTaskEvent> boolean refreshTask(String guildId, T taskEvent, GuildTaskItem taskItem, GuildTaskCfgItem cfg) {
		int loginCnt = LocalRedis.getInstance().getGuildShareCnt(guildId);
		int value = Math.min(loginCnt, cfg.getValue());
		taskItem.setValue(value);
		return true;
	}

}

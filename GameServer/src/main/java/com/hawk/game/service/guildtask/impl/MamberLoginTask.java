package com.hawk.game.service.guildtask.impl;

import com.hawk.game.service.GuildService;
import com.hawk.game.service.guildtask.GuildTask;
import com.hawk.game.service.guildtask.GuildTaskCfgItem;
import com.hawk.game.service.guildtask.GuildTaskEvent;
import com.hawk.game.service.guildtask.GuildTaskItem;
import com.hawk.game.service.guildtask.GuildTaskType;

/**
 * 联盟成员登录任务
 * 
 * @author jesse
 *
 */
@GuildTask(taskType = GuildTaskType.member_login)
public class MamberLoginTask implements IGuildTask {

	@Override
	public <T extends GuildTaskEvent> boolean refreshTask(String guildId, T taskEvent, GuildTaskItem taskItem, GuildTaskCfgItem cfg) {
		int loginCnt = GuildService.getInstance().getDailyLoginCnt(guildId);
		int value = Math.min(loginCnt, cfg.getValue());
		taskItem.setValue(value);
		return true;
	}

}

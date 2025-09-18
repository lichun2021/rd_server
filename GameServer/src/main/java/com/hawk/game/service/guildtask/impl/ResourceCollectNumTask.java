package com.hawk.game.service.guildtask.impl;

import java.util.List;

import com.hawk.game.service.guildtask.GuildTask;
import com.hawk.game.service.guildtask.GuildTaskCfgItem;
import com.hawk.game.service.guildtask.GuildTaskEvent;
import com.hawk.game.service.guildtask.GuildTaskItem;
import com.hawk.game.service.guildtask.GuildTaskType;
import com.hawk.game.service.guildtask.event.ResourceCollectCountEvent;

/**
 * 资源采集数量任务
 * 
 * @author jesse
 *
 */
@GuildTask(taskType = GuildTaskType.resource_collect)
public class ResourceCollectNumTask implements IGuildTask {

	@Override
	public <T extends GuildTaskEvent> boolean refreshTask(String guildId, T taskEvent, GuildTaskItem taskItem, GuildTaskCfgItem cfg) {
		ResourceCollectCountEvent event = (ResourceCollectCountEvent) taskEvent;

		int resourceType = event.getResourceType();
		List<Integer> conditions = cfg.getIds();

		// conditions为空 或 为0 代表任意的
		if (!conditions.isEmpty() && !conditions.contains(0) && !conditions.contains(resourceType)) {
			return false;
		}
		int value = Math.min(taskItem.getValue() + event.getCount(), cfg.getValue());
		taskItem.setValue(value);
		return true;
	}

}

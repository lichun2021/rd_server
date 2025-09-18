package com.hawk.game.service.guildtask.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.guildtask.GuildTaskEvent;
import com.hawk.game.service.guildtask.GuildTaskType;

/**
 * 资源采集数量事件
 * 
 * @author jesse
 *
 */
public class ResourceCollectCountEvent extends GuildTaskEvent {

	/** 资源类型 */
	int resourceType;

	/** 采集数量 */
	int count;

	public ResourceCollectCountEvent(String guildId, int resourceType, int count) {
		super(guildId);
		this.resourceType = resourceType;
		this.count = count;
	}

	public int getResourceType() {
		return resourceType;
	}

	public int getCount() {
		return count;
	}

	@Override
	public List<GuildTaskType> touchTasks() {
		List<GuildTaskType> touchTaskList = new ArrayList<GuildTaskType>();
		touchTaskList.add(GuildTaskType.resource_collect);
		return touchTaskList;
	}
}


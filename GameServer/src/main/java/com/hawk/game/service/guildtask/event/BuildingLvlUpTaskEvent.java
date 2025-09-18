package com.hawk.game.service.guildtask.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.guildtask.GuildTaskEvent;
import com.hawk.game.service.guildtask.GuildTaskType;

/**
 * 联盟成员完成建筑升级事件
 * 
 * @author jesse
 *
 */
public class BuildingLvlUpTaskEvent extends GuildTaskEvent {

	public BuildingLvlUpTaskEvent(String guildId) {
		super(guildId);
	}

	@Override
	public List<GuildTaskType> touchTasks() {
		List<GuildTaskType> touchTaskList = new ArrayList<GuildTaskType>();
		touchTaskList.add(GuildTaskType.building_up);
		return touchTaskList;
	}
}


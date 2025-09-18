package com.hawk.game.service.guildtask.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.guildtask.GuildTaskEvent;
import com.hawk.game.service.guildtask.GuildTaskType;

/**
 * 联盟成员完成联盟援助事件
 * 
 * @author jesse
 *
 */
public class GuildAssistTaskEvent extends GuildTaskEvent {

	public GuildAssistTaskEvent(String guildId) {
		super(guildId);
	}


	@Override
	public List<GuildTaskType> touchTasks() {
		List<GuildTaskType> touchTaskList = new ArrayList<GuildTaskType>();
		touchTaskList.add(GuildTaskType.guild_assist);
		return touchTaskList;
	}
}


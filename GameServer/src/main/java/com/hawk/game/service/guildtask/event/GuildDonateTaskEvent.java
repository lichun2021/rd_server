package com.hawk.game.service.guildtask.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.guildtask.GuildTaskEvent;
import com.hawk.game.service.guildtask.GuildTaskType;

/**
 * 联盟成员完成联盟捐献事件
 * 
 * @author jesse
 *
 */
public class GuildDonateTaskEvent extends GuildTaskEvent {

	public GuildDonateTaskEvent(String guildId) {
		super(guildId);
	}

	@Override
	public List<GuildTaskType> touchTasks() {
		List<GuildTaskType> touchTaskList = new ArrayList<GuildTaskType>();
		touchTaskList.add(GuildTaskType.guild_donate);
		return touchTaskList;
	}
}


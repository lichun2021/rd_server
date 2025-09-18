package com.hawk.game.service.guildtask.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.guildtask.GuildTaskEvent;
import com.hawk.game.service.guildtask.GuildTaskType;

/**
 * 联盟成员完成集结次数
 * 
 * @author jesse
 *
 */
public class MemberMasstCountEvent extends GuildTaskEvent {

	public MemberMasstCountEvent(String guildId) {
		super(guildId);
	}


	@Override
	public List<GuildTaskType> touchTasks() {
		List<GuildTaskType> touchTaskList = new ArrayList<GuildTaskType>();
		touchTaskList.add(GuildTaskType.guild_mass_atk);
		return touchTaskList;
	}
}


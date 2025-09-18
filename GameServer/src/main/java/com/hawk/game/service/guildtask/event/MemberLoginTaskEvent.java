package com.hawk.game.service.guildtask.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.guildtask.GuildTaskEvent;
import com.hawk.game.service.guildtask.GuildTaskType;

/**
 * 联盟成员登录事件
 * 
 * @author jesse
 *
 */
public class MemberLoginTaskEvent extends GuildTaskEvent {


	public MemberLoginTaskEvent(String guildId) {
		super(guildId);
	}


	@Override
	public List<GuildTaskType> touchTasks() {
		List<GuildTaskType> touchTaskList = new ArrayList<GuildTaskType>();
		touchTaskList.add(GuildTaskType.member_login);
		return touchTaskList;
	}
}


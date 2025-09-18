package com.hawk.game.service.guildtask.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.guildtask.GuildTaskEvent;
import com.hawk.game.service.guildtask.GuildTaskType;

/**
 * 联盟成员领取联盟宝藏奖励事件
 * 
 * @author jesse
 *
 */
public class GuildStorehouseRewardTaskEvent extends GuildTaskEvent {

	public GuildStorehouseRewardTaskEvent(String guildId) {
		super(guildId);
	}

	@Override
	public List<GuildTaskType> touchTasks() {
		List<GuildTaskType> touchTaskList = new ArrayList<GuildTaskType>();
		touchTaskList.add(GuildTaskType.guild_storehouse);
		return touchTaskList;
	}
}


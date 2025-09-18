package com.hawk.game.service.guildtask.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.guildtask.GuildTaskEvent;
import com.hawk.game.service.guildtask.GuildTaskType;

/**
 * 联盟成员野怪击杀事件
 * 
 * @author jesse
 *
 */
public class KillMonsterTaskEvent extends GuildTaskEvent {
	
	int atkTimes;

	public KillMonsterTaskEvent(String guildId) {
		super(guildId);
		this.atkTimes = 1;
	}
	
	public KillMonsterTaskEvent(String guildId, int atkTimes) {
		super(guildId);
		this.atkTimes = atkTimes;
	}

	@Override
	public List<GuildTaskType> touchTasks() {
		List<GuildTaskType> touchTaskList = new ArrayList<GuildTaskType>();
		touchTaskList.add(GuildTaskType.kill_monster);
		return touchTaskList;
	}

	public int getAtkTimes() {
		return atkTimes;
	}

	public void setAtkTimes(int atkTimes) {
		this.atkTimes = atkTimes;
	}
	
}


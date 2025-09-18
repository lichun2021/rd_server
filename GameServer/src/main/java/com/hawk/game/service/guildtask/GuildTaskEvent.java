package com.hawk.game.service.guildtask;

import java.util.List;

/**
 * 联盟任务事件
 * 
 * @author Jesse
 *
 */
public abstract class GuildTaskEvent {
	
	String guildId;
	
	

	public GuildTaskEvent(String guildId) {
		super();
		this.guildId = guildId;
	}
	

	public String getGuildId() {
		return guildId;
	}


	/**
	 * 触发任务列表
	 * @return
	 */
	public List<GuildTaskType> touchTasks() {
		return null;
	}
	
}

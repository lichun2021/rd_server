package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 联盟退出事件
 * @author hf
 */
public class GuildQuiteEvent extends ActivityEvent{
	private String guildId;
	private long joinGuildTime;
	public GuildQuiteEvent(){ super(null);}
	public GuildQuiteEvent(String playerId, String guildId, long joinGuildTime) {
		super(playerId);
		this.guildId = guildId;
		this.joinGuildTime = joinGuildTime;
	}

	public String getGuildId() {
		return guildId;
	}
	
	public long getJoinGuildTime() {
		return joinGuildTime;
	}
}

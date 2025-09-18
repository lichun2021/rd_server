package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 联盟解散
 * @author Jesse
 *
 */
public class GuildDismissEvent extends ActivityEvent {
	
	private String guildId;
	
	public GuildDismissEvent(){ super(null);}
	public GuildDismissEvent(String playerId, String guildId) {
		super(playerId);
		this.guildId = guildId;
	}

	public String getGuildId() {
		return guildId;
	}
}

package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;


public class CreateGuildEvent extends ActivityEvent {

	String guildId;
	
	public CreateGuildEvent(){ super(null);}
	public CreateGuildEvent(String playerId, String guildId) {
		super(playerId);
		this.guildId = guildId;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}
}

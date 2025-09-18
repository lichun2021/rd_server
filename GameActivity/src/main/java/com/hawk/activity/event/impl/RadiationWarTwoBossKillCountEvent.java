package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 辐射战争联盟击杀BOSS怪
 * 
 * @author che
 *
 */
public class RadiationWarTwoBossKillCountEvent extends ActivityEvent {
	
	private int guildKillCount;
	public RadiationWarTwoBossKillCountEvent(){ super(null);}
	public RadiationWarTwoBossKillCountEvent(String playerId, int guildKillCount) {
		super(playerId);
		this.guildKillCount = guildKillCount;
	}

	public int getGuildKillCount() {
		return guildKillCount;
	}
	
	
}

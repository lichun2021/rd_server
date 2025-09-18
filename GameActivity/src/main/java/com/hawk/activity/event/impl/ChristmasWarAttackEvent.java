package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 圣诞大战怪物
 * @author jm 
 */
public class ChristmasWarAttackEvent extends ActivityEvent {
	
	private String guildId;
	
	private int killCnt;
	
	public ChristmasWarAttackEvent(){ super(null);}
	public ChristmasWarAttackEvent(String playerId, String guildId, int killCnt) {
		super(playerId);
		this.guildId = guildId;
		this.killCnt = killCnt;		
	}

	public String getGuildId() {
		return guildId;
	}

	public int getKillCnt() {
		return killCnt;
	}

	public void setKillCnt(int killCnt) {
		this.killCnt = killCnt;
	}
}

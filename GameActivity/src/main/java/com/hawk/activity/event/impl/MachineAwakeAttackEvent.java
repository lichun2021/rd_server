package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 机甲觉醒攻击(伤害)事件
 * @author Jesse
 *
 */
public class MachineAwakeAttackEvent extends ActivityEvent {
	
	private String guildId;
	
	private int killCnt;
	
	public MachineAwakeAttackEvent(){ super(null);}
	public MachineAwakeAttackEvent(String playerId, String guildId, int killCnt) {
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

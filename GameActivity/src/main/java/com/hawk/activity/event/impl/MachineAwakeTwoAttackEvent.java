package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 机甲觉醒2(年兽)攻击(伤害)事件
 * @author Jesse
 *
 */
public class MachineAwakeTwoAttackEvent extends ActivityEvent {
	
	private String guildId;
	
	private int killCnt;
	
	public MachineAwakeTwoAttackEvent(){ super(null);}
	public MachineAwakeTwoAttackEvent(String playerId, String guildId, int killCnt) {
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

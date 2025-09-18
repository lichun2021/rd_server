package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

public class CommanderLevlUpMsg extends HawkMsg {
	/**
	 * 老的等级
	 */
	private int oldLevel;
	/**
	 * 新的等级
	 */
	private int newLevel;
	
	public CommanderLevlUpMsg(int oldLevel, int newLevel) {
		super();
		this.oldLevel = oldLevel;
		this.newLevel = newLevel;
	}
	
	public int getOldLevel() {
		return oldLevel;
	}
	public void setOldLevel(int oldLevel) {
		this.oldLevel = oldLevel;
	}
	public int getNewLevel() {
		return newLevel;
	}
	public void setNewLevel(int newLevel) {
		this.newLevel = newLevel;
	}
	
}

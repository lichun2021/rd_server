package com.hawk.game.item;

import com.hawk.game.item.mission.MissionEntityItem;

/**
 * 玩家成就
 * @author golden
 *
 */
public class PlayerAchieveItem extends MissionEntityItem {

	private int completeTime;
	
	public PlayerAchieveItem(int cfgId, int value, int state) {
		super(cfgId, value, state);
		this.completeTime = 0;
	}

	public PlayerAchieveItem(int cfgId, int value, int state, int completeTime) {
		super(cfgId, value, state);
		this.completeTime = completeTime;
	}
	
	public int getCompleteTime() {
		return completeTime;
	}

	public void setCompleteTime(int completeTime) {
		this.completeTime = completeTime;
	}

	public String toString() {
		return String.format("%d_%d_%d_%d", this.getCfgId(), this.getValue(), this.getState(), completeTime);
	}
	

}

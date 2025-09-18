package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

/**
 * 泰能强化第x次升级
 * @author Golden
 *
 */
public class PlantSoldierCrackChipMsg extends HawkMsg {
	int plantSoldierCrackChipTimes;
	int level = 0;
	
	public static PlantSoldierCrackChipMsg valueOf(int plantSoldierCrackChipTimes, int level) {
		PlantSoldierCrackChipMsg msg = new PlantSoldierCrackChipMsg();
		msg.plantSoldierCrackChipTimes = plantSoldierCrackChipTimes;
		msg.level = level;
		return msg;
	}

	public int getPlantSoldierCrackChipTimes() {
		return plantSoldierCrackChipTimes;
	}

	public int getLevel() {
		return level;
	}
}

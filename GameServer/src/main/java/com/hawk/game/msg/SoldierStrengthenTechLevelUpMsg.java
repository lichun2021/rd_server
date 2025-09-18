package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

/**
 * 升级泰能科技
 * @author Golden
 *
 */
public class SoldierStrengthenTechLevelUpMsg extends HawkMsg {
	int soldierId;
	int level;
	int group;
	
	public static SoldierStrengthenTechLevelUpMsg valueOf(int soldierId, int level, int group) {
		SoldierStrengthenTechLevelUpMsg msg = new SoldierStrengthenTechLevelUpMsg();
		msg.soldierId = soldierId;
		msg.level = level;
		msg.group = group;
		return msg;
	}

	public int getSoldierId() {
		return soldierId;
	}

	public int getLevel() {
		return level;
	}

	public int getGroup() {
		return group;
	}
}

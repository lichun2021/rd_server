package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

/**
 * 破译仪器升级
 * @author Golden
 *
 */
public class PlantInstrumentUpChipMsg extends HawkMsg {
	int level;
	
	public static PlantInstrumentUpChipMsg valueOf(int level) {
		PlantInstrumentUpChipMsg msg = new PlantInstrumentUpChipMsg();
		msg.level = level;
		return msg;
	}

	public int getLevel() {
		return level;
	}

}

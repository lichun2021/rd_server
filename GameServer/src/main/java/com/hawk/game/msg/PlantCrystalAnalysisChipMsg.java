package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

/**
 * 晶体分析msg
 * @author Golden
 *
 */
public class PlantCrystalAnalysisChipMsg extends HawkMsg {
	int level;
	
	public static PlantCrystalAnalysisChipMsg valueOf(int level) {
		PlantCrystalAnalysisChipMsg msg = new PlantCrystalAnalysisChipMsg();
		msg.level = level;
		return msg;
	}

	public int getLevel() {
		return level;
	}
}

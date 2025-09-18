package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.protocol.Const.BuildingType;

/**
 * 泰能强化第x次升级
 * @author Golden
 *
 */
public class PlantTechnologyChipMsg extends HawkMsg {
	BuildingType plantType;
	
	int plantTechnologyTimes;
	
	public static PlantTechnologyChipMsg valueOf(int plantTechnologyTimes, BuildingType plantType) {
		PlantTechnologyChipMsg msg = new PlantTechnologyChipMsg();
		msg.plantTechnologyTimes = plantTechnologyTimes;
		msg.plantType = plantType;
		return msg;
	}

	public int getPlantTechnologyTimes() {
		return plantTechnologyTimes;
	}

	public BuildingType getPlantType() {
		return plantType;
	}
}

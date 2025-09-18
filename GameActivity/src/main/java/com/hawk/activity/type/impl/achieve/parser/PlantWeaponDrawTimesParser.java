package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.PlantWeaponDrawTimesEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class PlantWeaponDrawTimesParser extends AchieveParser<PlantWeaponDrawTimesEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.PLANT_WEAPON_DRAW;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, PlantWeaponDrawTimesEvent event) {
		int oldTimes = achieveItem.getValue(0);
		achieveItem.setValue(0, oldTimes + event.getAddTimes());
		return true;
	}
}

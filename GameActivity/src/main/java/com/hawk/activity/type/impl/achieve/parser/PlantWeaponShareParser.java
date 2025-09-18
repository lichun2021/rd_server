package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.PlantWeaponShareEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class PlantWeaponShareParser extends AchieveParser<PlantWeaponShareEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.PLANT_WEAPON_SHARE;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, PlantWeaponShareEvent event) {
		int oldValue = achieveItem.getValue(0);
		achieveItem.setValue(0, oldValue + 1);
		return true;
	}
}

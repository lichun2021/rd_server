package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.PlantWeaponBackDrawEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class PlantWeaponBackDrawParser extends AchieveParser<PlantWeaponBackDrawEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.PLANT_WEAPON_BACK_DRAW;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, PlantWeaponBackDrawEvent event) {
		achieveItem.setValue(0, event.getTimes());
		return true;
	}
}

package com.hawk.activity.type.impl.achieve.parser;

import org.hawk.config.HawkConfigManager;

import com.hawk.activity.event.impl.PlantWeaponGetEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.plantweapon.cfg.PlantWeaponKVCfg;

public class PlantWeaponGetParser extends AchieveParser<PlantWeaponGetEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.PLANT_WEAPON_GET;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, PlantWeaponGetEvent event) {
		PlantWeaponKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(PlantWeaponKVCfg.class);
		if (event.getPlantWeaponId() != kvCfg.getPlantWeapon()) {
			return false;
		}
		int oldValue = achieveItem.getValue(0);
		achieveItem.setValue(0, oldValue + 1);
		return true;
	}
}

package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.EnterSuperWeaponEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class OccupySuperWeaponParser extends AchieveParser<EnterSuperWeaponEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.OCCUPY_SUPER_WEAPON;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, EnterSuperWeaponEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int totalVal = achieveItem.getValue(0)+ 1;
		if (totalVal > configValue) {
			totalVal = configValue;
		}
		achieveItem.setValue(0, totalVal);
		return true;
	}
}

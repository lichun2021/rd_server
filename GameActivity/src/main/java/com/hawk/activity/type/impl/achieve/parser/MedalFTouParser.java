package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.MedalFactoryTouEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class MedalFTouParser extends AchieveParser<MedalFactoryTouEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.MEDALF_TOU;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, MedalFactoryTouEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = 1 + achieveItem.getValue(0);
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}
}

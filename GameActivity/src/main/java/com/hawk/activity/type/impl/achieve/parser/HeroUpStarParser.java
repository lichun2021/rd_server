package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.HeroUpStarEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class HeroUpStarParser extends AchieveParser<HeroUpStarEvent> {
	@Override
	public AchieveType geAchieveType() {
		return AchieveType.HERO_STAR_UP;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, HeroUpStarEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = achieveItem.getValue(0) + (event.getNewStar() - event.getOldStar());
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}
}

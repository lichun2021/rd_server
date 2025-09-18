package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.PlanetExploreLevelUpEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class PlanetExploreLevelUpParser extends AchieveParser<PlanetExploreLevelUpEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.PLANET_EXPLORE_LEVEL_UP;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, PlanetExploreLevelUpEvent event) {
		int oldValue = achieveItem.getValue(0);
		achieveItem.setValue(0, oldValue + event.getTimes());
		return true;
	}
}

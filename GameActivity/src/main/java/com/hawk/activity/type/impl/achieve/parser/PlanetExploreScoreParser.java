package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.PlanetExploreScoreEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class PlanetExploreScoreParser extends AchieveParser<PlanetExploreScoreEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.PLANET_EXPLORE_PERSONAL_SCORE;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, PlanetExploreScoreEvent event) {
		achieveItem.setValue(0, event.getTotalScore());
		return true;
	}
}

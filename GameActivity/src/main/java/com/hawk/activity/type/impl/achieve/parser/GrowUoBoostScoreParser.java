package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.GrowUpBoostAddScoreEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class GrowUoBoostScoreParser extends AchieveParser<GrowUpBoostAddScoreEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.GROW_UP_BOOST_SCORE;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, GrowUpBoostAddScoreEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = Math.min(configValue, event.getScore());
		achieveItem.setValue(0, value);
		return true;
	}
}

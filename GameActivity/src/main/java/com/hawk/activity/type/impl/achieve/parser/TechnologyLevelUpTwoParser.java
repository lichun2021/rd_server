package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.TechnologyLevelUpEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class TechnologyLevelUpTwoParser extends AchieveParser<TechnologyLevelUpEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.TECHNOLOGY_LEVEL_UP_TWO;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, TechnologyLevelUpEvent event) {
		int source = achieveConfig.getConditionValue(0);
		int tchId = achieveConfig.getConditionValue(1);
		if (source != 0 && source != event.getSource() && tchId != 0 && tchId != event.getTechId()) {
			return false;
		}
		achieveItem.setValue(0, achieveItem.getValue(0) + 1);
		return true;
	}
}

package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.EnergiesSelfScoreEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class EnergiesSelfScoreParser extends AchieveParser<EnergiesSelfScoreEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.ENERGIES_SELF_SCORE;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, EnergiesSelfScoreEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = Math.max(event.getScore(), achieveItem.getValue(0));
		value = Math.min(value, configValue);
		achieveItem.setValue(0, value);
		return true;
	}
}

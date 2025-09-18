package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.CWScoreEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class SarboScoreGetParse extends AchieveParser<CWScoreEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.SARBO_SCORE_GET;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, CWScoreEvent event) {
		
		int configValue = achieveConfig.getConditionValue(0);
		int value = achieveItem.getValue(0) + (int)event.getScore();
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}
}

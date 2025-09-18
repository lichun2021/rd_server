package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.AddTavernScoreEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class GoldBabyDailyScoreParser extends AchieveParser<AddTavernScoreEvent>{
	
	@Override
	public AchieveType geAchieveType() {
		return AchieveType.GOLD_BABY_DAILY_SCORE;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, AddTavernScoreEvent event) {
		
		if (achieveItem.getValue(0) >= achieveConfig.getConditionValue(0)) {
			return false;
		}
		
		achieveItem.setValue(0, event.getTotalScore());
		if (achieveItem.getValue(0) > achieveConfig.getConditionValue(0)) {
			achieveItem.setValue(0, achieveConfig.getConditionValue(0));
		}
		return true;
	}
}

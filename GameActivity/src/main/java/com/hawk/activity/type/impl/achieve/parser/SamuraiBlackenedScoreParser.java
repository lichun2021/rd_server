package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.FighterPuzzleScoreEvent;
import com.hawk.activity.event.impl.SamuraiBlackenedScoreEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class SamuraiBlackenedScoreParser extends AchieveParser<SamuraiBlackenedScoreEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.SAMURAI_BLACKENED_SCORE;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, SamuraiBlackenedScoreEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = event.getScore();
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}
}

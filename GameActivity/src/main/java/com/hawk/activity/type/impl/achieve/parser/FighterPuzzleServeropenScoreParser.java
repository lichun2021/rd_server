package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.FighterPuzzleServeropenScoreEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class FighterPuzzleServeropenScoreParser extends AchieveParser<FighterPuzzleServeropenScoreEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.FIGHTER_PUZZLE_SCORE;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, FighterPuzzleServeropenScoreEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = event.getScore();
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}
}

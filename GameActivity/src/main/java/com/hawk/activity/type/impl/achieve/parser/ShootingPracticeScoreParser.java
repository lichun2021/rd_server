package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.ShootingPracticeScoreEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class ShootingPracticeScoreParser extends AchieveParser<ShootingPracticeScoreEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.SHOOT_PRACTICE_SCORE;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, ShootingPracticeScoreEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = event.getScore();
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}
}

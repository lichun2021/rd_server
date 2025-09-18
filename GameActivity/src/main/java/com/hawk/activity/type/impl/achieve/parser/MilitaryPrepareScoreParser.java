package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.AddMilitaryPrepareScoreEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 军事战备积分
 * @author Winder
 *
 */
public class MilitaryPrepareScoreParser extends AchieveParser<AddMilitaryPrepareScoreEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.MILITARY_PREPARE_SCORE;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, AddMilitaryPrepareScoreEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = achieveItem.getValue(0) + event.getScore();
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}
}

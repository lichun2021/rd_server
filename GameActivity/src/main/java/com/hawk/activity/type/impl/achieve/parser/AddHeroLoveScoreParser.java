package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.AddHeroLoveScoreEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 英雄委任增加积分.
 * @author jm
 *
 */
public class AddHeroLoveScoreParser extends AchieveParser<AddHeroLoveScoreEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.HERO_LOVE_SCORE;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, AddHeroLoveScoreEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = achieveItem.getValue(0) + event.getScore();
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}
}

package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.DragonBoatCelebrationScoreEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class DragonBoatCelebrationScoreParser extends AchieveParser<DragonBoatCelebrationScoreEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.DRAGON_BOAT_CELEBRATION_GUILD_SCORE;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, DragonBoatCelebrationScoreEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int score = (int) (event.getScore() > Integer.MAX_VALUE ? Integer.MAX_VALUE : event.getScore());
		int value = Math.max(score, achieveItem.getValue(0));
		value = Math.min(value, configValue);
		achieveItem.setValue(0, value);
		return true;
	}
}

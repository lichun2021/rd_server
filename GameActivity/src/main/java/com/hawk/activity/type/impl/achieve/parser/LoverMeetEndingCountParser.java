package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.LoverMeetEndingCountEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class LoverMeetEndingCountParser extends AchieveParser<LoverMeetEndingCountEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.LOVER_MEET_ENDING_COUNT;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, LoverMeetEndingCountEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = event.getCount();
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}
}

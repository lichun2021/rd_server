package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.CelebrationCourseSignEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class CelebrationCourseSignParser extends AchieveParser<CelebrationCourseSignEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.CELEBRATION_COURSE_SIGN;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, CelebrationCourseSignEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = event.getN()+achieveItem.getValue(0);
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}
}

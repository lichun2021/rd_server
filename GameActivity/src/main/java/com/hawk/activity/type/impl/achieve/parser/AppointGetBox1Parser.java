package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.AppointGetBox1Event;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class AppointGetBox1Parser extends AchieveParser<AppointGetBox1Event> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.Appoint_Get331001;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, AppointGetBox1Event event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = achieveItem.getValue(0) + event.getScore();
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}
}

package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.LoginDayGoldBabyEvent;
import com.hawk.activity.event.impl.LoginDaysActivityEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class LoginDayGoldBabyParser extends AchieveParser<LoginDayGoldBabyEvent>{
	
	@Override
	public AchieveType geAchieveType() {
		return AchieveType.LOGIN_DAYS_GOLD_BABY;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, LoginDayGoldBabyEvent event) {
		if (achieveItem.getValue(0) >= achieveConfig.getConditionValue(0)) {
			return false;
		}
		achieveItem.setValue(0, event.getLoginDays());
		if (achieveItem.getValue(0) > achieveConfig.getConditionValue(0)) {
			achieveItem.setValue(0, achieveConfig.getConditionValue(0));
		}
		return true;
	}
}

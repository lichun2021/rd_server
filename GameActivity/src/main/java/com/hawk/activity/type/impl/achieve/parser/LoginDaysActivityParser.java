package com.hawk.activity.type.impl.achieve.parser;

import org.hawk.log.HawkLog;

import com.hawk.activity.event.impl.LoginDaysActivityEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class LoginDaysActivityParser extends AchieveParser<LoginDaysActivityEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.LOGIN_DAYS_ACTIVITY;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, LoginDaysActivityEvent event) {
		HawkLog.logPrintln("LoginDaysActivityParser updateAchieve, playerId: {}, achieveId: {}, value: {}, configValue: {}, eventValue: {}, activityType: {}", 
				event.getPlayerId(), achieveItem.getAchieveId(), achieveItem.getValue(0), achieveConfig.getConditionValue(0), event.getLoginDays(), event.getActivityType());
		int configValue = achieveConfig.getConditionValue(0);
		int value = event.getLoginDays();
		if(achieveItem.getValue(0) >= value){
			return false;
		}
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}
}

package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.LoginDayFestivalTwoEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class LoginDaysFestivalTwoParser extends AchieveParser<LoginDayFestivalTwoEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.LOGIN_DAYS_FESTIVAL_TWO;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, LoginDayFestivalTwoEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = event.getLoginDays();
		if(achieveItem.getValue(0) >= value){
			return false;
		}
		if (value > configValue) {
			value = configValue;
		}
		if (value > 0) {
			achieveItem.setValue(0, value);
		}
		return true;
	}
}

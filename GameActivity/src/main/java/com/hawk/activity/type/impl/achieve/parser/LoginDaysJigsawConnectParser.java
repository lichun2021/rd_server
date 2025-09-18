package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.LoginDayJigsawConnectEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 双十一拼图活动 登录
 */
public class LoginDaysJigsawConnectParser extends AchieveParser<LoginDayJigsawConnectEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.LOGIN_DAYS_JIGSAW_CONNECT;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, LoginDayJigsawConnectEvent event) {
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

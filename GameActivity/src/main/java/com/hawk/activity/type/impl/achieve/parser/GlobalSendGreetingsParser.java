package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.GlobalSendGreetingsEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class GlobalSendGreetingsParser extends AchieveParser<GlobalSendGreetingsEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.GLOBAL_SEND_GREETINGS;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, GlobalSendGreetingsEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = event.getNum();
		int totalVal = achieveItem.getValue(0)+ value;
		if (totalVal > configValue) {
			totalVal = configValue;
		}
		achieveItem.setValue(0, totalVal);
		return true;
	}
}

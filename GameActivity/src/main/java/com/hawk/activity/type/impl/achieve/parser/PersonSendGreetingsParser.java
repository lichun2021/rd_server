package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.PersonSendGreetingsEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class PersonSendGreetingsParser extends AchieveParser<PersonSendGreetingsEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.PERSON_SEND_GREETINGS;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, PersonSendGreetingsEvent event) {
		
		int configValue = achieveConfig.getConditionValue(0);
		int value = achieveItem.getValue(0) + event.getNum();
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}
}

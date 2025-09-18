package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.PlayerSignEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class SignInParser extends AchieveParser<PlayerSignEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.SIGN_IN_TIMES;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, PlayerSignEvent event) {
		int day = achieveItem.getValue(0);
		achieveItem.setValue(0, day + 1);
		return true;
	}
}

package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.MakeTrapEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class TavernMakeTrapParser extends AchieveParser<MakeTrapEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.TAVERN_MAKE_TRAP_COUNT;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {

		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, MakeTrapEvent event) {
		int trapId = achieveConfig.getConditionValue(0);
		if (trapId > 0 && trapId != event.getTrapId()) {
			return false;
		}
		
		int count = achieveData.getValue(0) + event.getCount();
		achieveData.setValue(0, count);
		return true;
	}
}

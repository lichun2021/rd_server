package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.WishingEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class WishingParser extends AchieveParser<WishingEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.WISHING_TIMES;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, WishingEvent event) {
		achieveItem.setValue(0, achieveItem.getValue(0) + 1);
		return true;
	}
}

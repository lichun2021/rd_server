package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.DragonBoatLuckyBagOpenEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class DragonBoatLuckyBagOpenParser extends AchieveParser<DragonBoatLuckyBagOpenEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.DRAGON_BOAT_LUCKY_BAG_OPEN_COUNT;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, DragonBoatLuckyBagOpenEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = achieveItem.getValue(0) + event.getOpenCount();
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}
}

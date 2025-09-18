package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.DailyBuyItemCountEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class DailyBuyGiftItemCountParser extends AchieveParser<DailyBuyItemCountEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.DAILY_BUY_GIFT_ITEM_COUNT;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, DailyBuyItemCountEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = event.getCount();
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

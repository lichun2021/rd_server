package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.TravelShopAssistAchieveFinishEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class TravelShopAchieveFinishParser extends AchieveParser<TravelShopAssistAchieveFinishEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.TRAVEL_SHOP_ASSIST_ASSIST_ACHIEVE_FINISH;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, TravelShopAssistAchieveFinishEvent event) {
		achieveItem.setValue(0, achieveItem.getValue(0) + event.getFinishNum());
		return true;
	}
}

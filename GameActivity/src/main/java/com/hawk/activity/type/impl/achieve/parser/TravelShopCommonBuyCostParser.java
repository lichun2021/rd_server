package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.TravelShopPurchaseEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class TravelShopCommonBuyCostParser extends AchieveParser<TravelShopPurchaseEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.TRAVEL_SHOP_COMMON_COST;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, TravelShopPurchaseEvent event) {
		if (!event.isCommonPool()) {
			return false;
		}
		if (!event.isInActivity()) {
			return false;
		}
		int costType = achieveConfig.getConditionValue(0);
		if (event.getCostType() != costType) {
			return false;
		}
		achieveItem.setValue(0, achieveItem.getValue(0) + event.getCostNum());
		return true;
	}
}

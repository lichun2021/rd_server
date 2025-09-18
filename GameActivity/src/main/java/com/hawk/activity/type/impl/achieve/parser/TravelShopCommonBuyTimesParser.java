package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.TravelShopPurchaseEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class TravelShopCommonBuyTimesParser extends AchieveParser<TravelShopPurchaseEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.TRAVEL_SHOP_COMMON_BUY_TIMES;
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
		achieveItem.setValue(0, achieveItem.getValue(0) + 1);
		return true;
	}
}

package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.TravelShopBuyBlackGoldPackageEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class TravelShopBuyBlackGoldPackageParser extends AchieveParser<TravelShopBuyBlackGoldPackageEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.TRAVEL_SHOP_BLACK_GOLD_PACKAGE_BUY_TIMES;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, TravelShopBuyBlackGoldPackageEvent event) {
		achieveItem.setValue(0, achieveItem.getValue(0) + event.getNum());
		return true;
	}
}

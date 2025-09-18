package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.TravelShopPurchaseEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.game.protocol.Const.PlayerAttr;

public class TravelShopBuyUseGoldParser extends AchieveParser<TravelShopPurchaseEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.TRAVEL_SHOP_PUCHASE_GOLD_TIMES;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, TravelShopPurchaseEvent event) {
		//Old
		if (!event.isCommonPool()) {
			return false;
		}
		if (event.getCostType() != PlayerAttr.GOLD_VALUE) {
			return false;
		}
		achieveItem.setValue(0, achieveItem.getValue(0) + 1);
		return true;
	}
}

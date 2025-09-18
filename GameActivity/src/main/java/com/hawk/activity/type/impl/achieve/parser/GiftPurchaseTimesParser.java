package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.GiftPurchasePriceEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 购买超值礼包次数
 * 
 * @author lating
 *
 */
public class GiftPurchaseTimesParser extends AchieveParser<GiftPurchasePriceEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.SALE_GIFT_PURCHASE_TIMES;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, GiftPurchasePriceEvent event) {
		achieveItem.setValue(0, achieveItem.getValue(0) + event.getTimes());
		return true;
	}
}

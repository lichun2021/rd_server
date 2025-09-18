package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.BuyMonthCardEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 购买月卡消耗人民币
 * 
 * @author lating
 *
 */
public class MonthCardPurchaseParser extends AchieveParser<BuyMonthCardEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.MONTH_CARD_PURCHASE_CONSUME;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, BuyMonthCardEvent event) {
		achieveItem.setValue(0, achieveItem.getValue(0) + event.getConsumeMoney());
		return true;
	}
}

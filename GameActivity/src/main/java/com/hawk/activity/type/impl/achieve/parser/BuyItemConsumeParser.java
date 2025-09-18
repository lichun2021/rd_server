package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.BuyItemConsumeEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 购买每日必买礼包消耗
 * 
 * @author lating
 *
 */
public class BuyItemConsumeParser extends AchieveParser<BuyItemConsumeEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.BUY_ITEM_CONSUME;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, BuyItemConsumeEvent event) {
		achieveItem.setValue(0, achieveItem.getValue(0) + event.getCostMoney());
		return true;
	}
}

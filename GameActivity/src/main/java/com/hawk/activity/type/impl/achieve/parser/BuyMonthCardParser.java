package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.BuyMonthCardEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class BuyMonthCardParser extends AchieveParser<BuyMonthCardEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.BUY_MONTH_CARD;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, BuyMonthCardEvent event) {
		int before = achieveData.getValue(0);
		achieveData.setValue(0, before + 1);
		return true;
	}

}

package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.BestPrizeDrawEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class BestPrizeDrawParser extends AchieveParser<BestPrizeDrawEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.BEST_PRIZE_DRAW_CONSUME;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, BestPrizeDrawEvent event) {
		achieveItem.setValue(0, event.getDrawConsume());
		return true;
	}
}

package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.LotteryDrawEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class LotteryDrawTimesParser extends AchieveParser<LotteryDrawEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.LOTTERY_DRAY_TIMES;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, LotteryDrawEvent event) {
		int value = event.getTimes();
		achieveItem.setValue(0, achieveItem.getValue(0) + value);
		return true;
	}
}

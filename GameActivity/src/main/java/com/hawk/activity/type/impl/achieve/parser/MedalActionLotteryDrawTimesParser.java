package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.MedalActionLotteryDrawEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class MedalActionLotteryDrawTimesParser extends AchieveParser<MedalActionLotteryDrawEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.MEDAL_TREASURE_LOTTERY_TIMES;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, MedalActionLotteryDrawEvent event) {
		int value = event.getTimes();
		achieveItem.setValue(0, achieveItem.getValue(0) + value);
		return true;
	}
}

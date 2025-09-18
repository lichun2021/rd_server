package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.LuckyStarLotteryEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class LuckyStarLotteryParser extends AchieveParser<LuckyStarLotteryEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.LUCKY_STAR_LOTTERY_TIMES;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, LuckyStarLotteryEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = Math.min(configValue, event.getLotteryTotal());
		achieveItem.setValue(0, value);
		return true;
	}
}

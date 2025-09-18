package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.HonourMobilizeCountEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class HonourMobilizeLotteryCountParser extends AchieveParser<HonourMobilizeCountEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.HONOUR_MOBILIZE_LOTTERY_COUNT;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, HonourMobilizeCountEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int count = event.getCount();
		if (count > configValue) {
			count = configValue;
		}
		achieveItem.setValue(0, count);
		return true;
	}
}

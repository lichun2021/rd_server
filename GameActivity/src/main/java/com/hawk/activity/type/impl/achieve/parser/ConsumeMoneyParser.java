package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.ConsumeMoneyEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class ConsumeMoneyParser extends AchieveParser<ConsumeMoneyEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.CONSUME_MONEY;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, ConsumeMoneyEvent event) {
		int resType = achieveConfig.getConditionValue(0);
		if (resType != event.getResType()) {
			return false;
		}
		int configValue = achieveConfig.getConditionValue(1);
		int value = achieveItem.getValue(0) + (int) event.getNum();
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}
}

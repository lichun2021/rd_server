package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.DeepTreasureOpenBoxEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class DeepTreasureBoxTimesParser extends AchieveParser<DeepTreasureOpenBoxEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.DEEP_TREASURE_BOX_TIMES;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, DeepTreasureOpenBoxEvent event) {
		int value = event.getTimes();
		int configValue = achieveConfig.getConditionValue(0);
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}
}

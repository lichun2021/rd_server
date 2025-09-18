package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.VitCostEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class VitCostParser extends AchieveParser<VitCostEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.VI_COST;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, VitCostEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = event.getCost() + achieveItem.getValue(0);
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}
}

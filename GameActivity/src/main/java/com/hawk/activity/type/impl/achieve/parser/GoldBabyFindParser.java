package com.hawk.activity.type.impl.achieve.parser;


import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.sun.accessibility.internal.resources.accessibility;

public class GoldBabyFindParser extends AchieveParser<GoldBabyFindTimesEvent>{
	
	@Override
	public AchieveType geAchieveType() {
		return AchieveType.GOLD_BABY_CUMULATIVE_FIND;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, GoldBabyFindTimesEvent event) {
		if (achieveItem.getValue(0) >= achieveConfig.getConditionValue(0)) {
			return false;
		}
		achieveItem.setValue(0, event.getTimes());
		if (achieveItem.getValue(0) > achieveConfig.getConditionValue(0)) {
			achieveItem.setValue(0, achieveConfig.getConditionValue(0));
		}
		return true;
	}
}

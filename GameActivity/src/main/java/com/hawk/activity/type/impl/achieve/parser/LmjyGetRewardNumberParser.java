package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.LmjyGetRewardEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class LmjyGetRewardNumberParser  extends AchieveParser<LmjyGetRewardEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.LMJY_REWARD_NUMBER;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, LmjyGetRewardEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		//直接计镒数
		int totalVal = achieveItem.getValue(0)+ 1;
		if (totalVal > configValue) {
			totalVal = configValue;
		}
		achieveItem.setValue(0, totalVal);
		return true;
	}

}

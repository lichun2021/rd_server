package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.TWScoreEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class TiberiumJoinNumberParser extends AchieveParser<TWScoreEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.TIBERIUM_JOIN_NUMBER;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, TWScoreEvent event) {
		if(event.getEnterTime()<=0){
			return false;
		}
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

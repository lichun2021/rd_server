package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.TrainSoldierCompleteEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class TrainSoldierCompleteParser extends AchieveParser<TrainSoldierCompleteEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.TRAIN_SOLDIER_COMPLETE_NUM;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, TrainSoldierCompleteEvent event) {
		int soldierId = achieveConfig.getConditionValue(0);
		if (soldierId > 0 && event.getTrainId() != soldierId) {
			return false;
		}
		int configNum = achieveConfig.getConditionValue(1);
		int num = event.getNum() + achieveItem.getValue(0);
		if (num > configNum) {
			num = configNum;
		}
		achieveItem.setValue(0, num);
		return true;
	}
}

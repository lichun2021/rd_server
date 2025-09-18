package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.SoldierNumChangeEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class SoldierHaveNumParser extends AchieveParser<SoldierNumChangeEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.TRAIN_SOLDIER_HAVE_NUM;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		ActivityDataProxy dataGeter = PlayerDataHelper.getInstance().getDataGeter();
		int armyId = achieveConfig.getConditionValue(0);
		int soldierHaveNum = dataGeter.getSoldierHaveNum(playerId, armyId);
		int configNum = achieveConfig.getConditionValue(1);
		if (soldierHaveNum > configNum) {
			soldierHaveNum = configNum;
		}
		achieveItem.setValue(0, soldierHaveNum);
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, SoldierNumChangeEvent event) {
		int soldierId = achieveConfig.getConditionValue(0);
		if (soldierId > 0 && event.getTrainId() != soldierId) {
			return false;
		}
		int configValue = achieveConfig.getConditionValue(1);
		int value = achieveItem.getValue(0) + event.getNum();
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}
}


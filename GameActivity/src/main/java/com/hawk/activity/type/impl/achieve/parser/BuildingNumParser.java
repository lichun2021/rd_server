package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.BuildingCreateEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class BuildingNumParser extends AchieveParser<BuildingCreateEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.BUILD_NUM;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		ActivityDataProxy dataGeter = PlayerDataHelper.getInstance().getDataGeter();
		int buildType = achieveConfig.getConditionValue(0);
		int configNum = achieveConfig.getConditionValue(1);
		int buildingNum = dataGeter.getBuildingNum(playerId, buildType);
		if (buildingNum > configNum) {
			buildingNum = configNum;
		}
		achieveItem.setValue(0, buildingNum);
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, BuildingCreateEvent event) {
		int buildType = achieveConfig.getConditionValue(0);
		if (event.getBuildType() != buildType) {
			return false;
		}
		int configValue = achieveConfig.getConditionValue(1);
		int value = event.getNum();
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}

}

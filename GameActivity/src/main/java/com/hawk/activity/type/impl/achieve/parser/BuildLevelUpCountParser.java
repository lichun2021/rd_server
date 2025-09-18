package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.BuildingLevelUpEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class BuildLevelUpCountParser extends AchieveParser<BuildingLevelUpEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.BUILD_LEVEL_UP_COUNT;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, BuildingLevelUpEvent event) {
		if (event.isLogin() || event.getIsSpread()) {
			return false;
		}
		int configBuildType = achieveConfig.getConditionValue(0);
		if (configBuildType > 0 && event.getBuildType() != configBuildType) {
			return false;
		}
		int oldCount = achieveItem.getValue(0);
		achieveItem.setValue(0, oldCount + 1);
		return true;
	}

}

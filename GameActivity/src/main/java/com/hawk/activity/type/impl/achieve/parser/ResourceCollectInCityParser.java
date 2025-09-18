package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.CityResourceCollectEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class ResourceCollectInCityParser extends AchieveParser<CityResourceCollectEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.RESOURCE_COLLECT_IN_CITY;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, CityResourceCollectEvent event) {
		int resourceType = achieveConfig.getConditionValue(0);
		Double collectNum = event.getCollectNum(resourceType);
		if (collectNum == null || collectNum <= 0) {
			return false;
		}
		int configNum = achieveConfig.getConditionValue(1);
		int num = (int) (event.getCollectNum(resourceType) + achieveItem.getValue(0));
		if (num > configNum) {
			num = configNum;
		}
		achieveItem.setValue(0, num);
		return true;
	}
}

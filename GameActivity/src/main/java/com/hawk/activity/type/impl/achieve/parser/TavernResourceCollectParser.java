package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.ResourceCollectEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class TavernResourceCollectParser extends AchieveParser<ResourceCollectEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.TAVERN_RESOURCE_COLLECT;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, ResourceCollectEvent event) {
		int resourceType = achieveConfig.getConditionValue(0);
		Double collectNum = event.getCollectNum(resourceType);
		if (collectNum == null || collectNum <= 0) {
			return false;
		}
		int num = (int) (event.getCollectNum(resourceType) + achieveItem.getValue(0));
		achieveItem.setValue(0, num);
		return true;
	}
}

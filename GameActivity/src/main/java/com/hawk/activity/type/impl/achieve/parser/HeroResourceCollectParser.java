package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.HeroResourceCollectEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class HeroResourceCollectParser extends AchieveParser<HeroResourceCollectEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.HERO_MARCH_COLLECT_RESOURCE;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, HeroResourceCollectEvent event) {
		
		int count = achieveData.getValue(0) + 1;
		if (count > achieveConfig.getConditionValue(0)) {
			return false;
		}
		achieveData.setValue(0, count);
		return true;
	}

}

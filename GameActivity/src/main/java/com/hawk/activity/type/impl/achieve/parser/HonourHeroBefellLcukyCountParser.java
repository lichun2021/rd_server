package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.HonourHeroBefellLuckyCountEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class HonourHeroBefellLcukyCountParser extends AchieveParser<HonourHeroBefellLuckyCountEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.HONOUR_HERO_BEFELL_LUCKY_COUNT;
	}
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}
	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig,
			HonourHeroBefellLuckyCountEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = event.getCount();
		if (value >= configValue) {
			value = configValue;
		}
		achieveData.setValue(0, value);
		return true;
	}
}

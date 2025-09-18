package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.RadiationWarTwoBossKillCountEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class RadiationWarTwoKillBossCountParser extends AchieveParser<RadiationWarTwoBossKillCountEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.RADIATIO_NWAR_TWO_BOSS_KILL_COUNT;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, RadiationWarTwoBossKillCountEvent event) {
		int configValue = achieveConfig.getConditionValue(achieveConfig.getConditionValues().size() - 1);
		int value = achieveItem.getValue(0);
		if(value >= event.getGuildKillCount()){
			return false;
		}
		value = event.getGuildKillCount();
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}
}

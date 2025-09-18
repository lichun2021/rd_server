package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.AttackFoggyEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class AttackFoggyParser extends AchieveParser<AttackFoggyEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.ATTACK_FOGGY;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, AttackFoggyEvent event) {
		achieveData.setValue(0, achieveData.getValue(0) + 1);
		return true;
	}

}

package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.CastSkillEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class CastSkillParser extends AchieveParser<CastSkillEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.CAST_SKILL;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, CastSkillEvent event) {
		achieveData.setValue(0, achieveData.getValue(0) + 1);
		return true;
	}

}

package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.AgencyMonsterAtkWinEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class AgencyMonsterAtckParser extends AchieveParser<AgencyMonsterAtkWinEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.AGENCY_MONSTER_ATK_WIN;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, AgencyMonsterAtkWinEvent event) {
		int oldValue = achieveItem.getValue(0);
		achieveItem.setValue(0, oldValue + event.getTimes());
		return true;
	}
}

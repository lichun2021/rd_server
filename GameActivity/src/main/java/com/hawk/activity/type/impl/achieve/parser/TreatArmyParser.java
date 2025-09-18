package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.TreatArmyEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class TreatArmyParser extends AchieveParser<TreatArmyEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.TREAT_ARMY;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, TreatArmyEvent event) {
		int value = achieveItem.getValue(0) + event.getCount();
		achieveItem.setValue(0, value);
		return true;
	}
}

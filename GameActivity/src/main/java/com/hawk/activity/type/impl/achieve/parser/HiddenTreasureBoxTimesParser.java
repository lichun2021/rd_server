package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.HiddenTreasureOpenBoxEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class HiddenTreasureBoxTimesParser extends AchieveParser<HiddenTreasureOpenBoxEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.HIDDEN_TEASURE_BOX_TIMES;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, HiddenTreasureOpenBoxEvent event) {
		int value = event.getTimes();
		achieveItem.setValue(0, value);
		return true;
	}
}

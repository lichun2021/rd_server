package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.NewbieTrainEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.game.protocol.Activity.NoviceTrainType;

public class NewbieHeroTrainTimesParser extends AchieveParser<NewbieTrainEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.NOVICE_HERO_TRAINING_TIMES;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, NewbieTrainEvent event) {
		if (event.getType() == NoviceTrainType.TYPE_HERO_VALUE) {
			int value = event.getTimes();
			achieveItem.setValue(0, achieveItem.getValue(0) + value);
		}
		return true;
	}
}

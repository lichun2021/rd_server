package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.ShootingPracticeBuyTimesEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class ShootingPracticeBuyTimeParser extends AchieveParser<ShootingPracticeBuyTimesEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.SHOOT_PRACTICE_BUY_GAME_COUNT;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, ShootingPracticeBuyTimesEvent event) {
		int oldTimes = achieveItem.getValue(0);
		achieveItem.setValue(0, oldTimes + event.getAddTimes());
		return true;
	}
}

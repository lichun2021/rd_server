package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.DoRouletteEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 时空轮盘
 * @author Winder
 *
 */
public class DoRouletteParser extends AchieveParser<DoRouletteEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.DO_ROULETTE_TIMES;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, DoRouletteEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = event.getCount();
		int totalVal = achieveItem.getValue(0) + value;
		if (totalVal > configValue) {
			totalVal = configValue;
		}
		achieveItem.setValue(0, totalVal);
		return true;
	}

}

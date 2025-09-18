package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.CrScoreEvent;
import com.hawk.activity.event.impl.DoRouletteEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 冠军试炼次数
 * @author Winder
 */
public class DoCrScoreParser extends AchieveParser<CrScoreEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.DO_CR_TIMES;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, CrScoreEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = 1;
		int totalVal = achieveItem.getValue(0) + value;
		if (totalVal > configValue) {
			totalVal = configValue;
		}
		achieveItem.setValue(0, totalVal);
		return true;
	}

}
